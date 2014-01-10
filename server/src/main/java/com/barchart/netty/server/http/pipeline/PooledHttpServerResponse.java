/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.server.http.request.HttpServerResponse;

/**
 * Not thread safe.
 */
public class PooledHttpServerResponse extends DefaultFullHttpResponse implements
		HttpServerResponse {

	private static final Logger log = LoggerFactory
			.getLogger(PooledHttpServerResponse.class);

	private final Collection<Cookie> cookies = new HashSet<Cookie>();

	private KeepaliveHelper keepaliveHelper;
	private ChannelHandlerContext context;
	private PooledHttpServerRequest request;

	private OutputStream out;
	private Writer writer;

	private Charset charSet = CharsetUtil.UTF_8;

	private boolean started = false;
	private boolean finished = false;

	public PooledHttpServerResponse() {
		super(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
	}

	void init(final ChannelHandlerContext context_,
			final KeepaliveHelper keepaliveHelper_,
			final PooledHttpServerRequest request_) {

		// Prevent underlying ByteBuf from being collected between requests
		retain();

		// Reset default request values if this is a recycled handler
		if (finished) {
			headers().clear();
			content().clear();
			setStatus(HttpResponseStatus.OK);
		}

		context = context_;
		request = request_;
		keepaliveHelper = keepaliveHelper_;

		charSet = CharsetUtil.UTF_8;

		finished = false;
		started = false;

		out = new ByteBufOutputStream(content());
		writer = new OutputStreamWriter(out, charSet);

	}

	@Override
	public OutputStream getOutputStream() {
		return out;
	}

	@Override
	public Writer getWriter() {
		return writer;
	}

	@Override
	public void setCookie(final Cookie cookie) {
		cookies.add(cookie);
	}

	@Override
	public void setCookie(final String name, final String value) {
		cookies.add(new DefaultCookie(name, value));
	}

	@Override
	public void sendRedirect(final String location) {
		headers().set(HttpHeaders.Names.LOCATION, location);
	}

	@Override
	public PooledHttpServerResponse setProtocolVersion(final HttpVersion version) {
		super.setProtocolVersion(version);
		return this;
	}

	@Override
	public PooledHttpServerResponse setStatus(final HttpResponseStatus status) {
		super.setStatus(status);
		return this;
	}

	@Override
	public void setCharacterEncoding(final String charSet_) {
		charSet = Charset.forName(charSet_);
		writer = new OutputStreamWriter(out, charSet);
	}

	@Override
	public Charset getCharacterEncoding() {
		return charSet;
	}

	@Override
	public void setContentLength(final int length) {
		HttpHeaders.setContentLength(this, length);
	}

	@Override
	public void setContentType(final String mimeType) {
		headers().set(HttpHeaders.Names.CONTENT_TYPE, mimeType);
	}

	@Override
	public boolean isChunkedEncoding() {
		return HttpHeaders.isTransferEncodingChunked(this);
	}

	@Override
	public void setChunkedEncoding(final boolean chunked) {

		if (chunked != isChunkedEncoding()) {

			if (chunked) {

				HttpHeaders.setTransferEncodingChunked(this);
				out = new HttpChunkOutputStream(context);
				writer = new OutputStreamWriter(out, charSet);

			} else {

				HttpHeaders.removeTransferEncodingChunked(this);
				out = new ByteBufOutputStream(content());
				writer = new OutputStreamWriter(out, charSet);

			}

		}

	}

	@Override
	public void write(final String data) throws IOException {
		if (data != null) {
			write(data.getBytes());
		}
	}

	@Override
	public void write(final byte[] data) throws IOException {

		checkFinished();

		out.write(data);
		out.flush();

	}

	@Override
	public void write(final byte[] data, final int offset, final int length)
			throws IOException {

		checkFinished();

		out.write(data, offset, length);
		out.flush();

	}

	@Override
	public long writtenBytes() {
		if (out instanceof ByteBufOutputStream) {
			return ((ByteBufOutputStream) out).writtenBytes();
		} else if (out instanceof HttpChunkOutputStream) {
			return ((HttpChunkOutputStream) out).writtenBytes();
		}
		return 0;
	}

	private ChannelFuture startResponse() {

		checkFinished();

		if (started) {
			throw new IllegalStateException("Response already started");
		}

		// Set headers
		headers().set(HttpHeaders.Names.SET_COOKIE,
				ServerCookieEncoder.encode(cookies));

		if (!isChunkedEncoding()) {
			setContentLength(content().readableBytes());
		}

		if (HttpHeaders.isKeepAlive(request)) {
			headers().set(HttpHeaders.Names.CONNECTION,
					HttpHeaders.Values.KEEP_ALIVE);
		}

		started = true;

		return context.writeAndFlush(this);

	}

	@Override
	public ChannelFuture finish() throws IOException {

		checkFinished();

		ChannelFuture writeFuture = null;

		// Handlers might call finish() on a cancelled/closed
		// channel, don't cause unnecessary pipeline exceptions
		if (context.channel().isOpen()) {

			if (isChunkedEncoding()) {

				if (!started) {
					log.debug("Warning, empty response");
					startResponse();
				}

				writeFuture =
						context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

			} else {

				writeFuture = startResponse();

			}

		}

		close();

		if (writeFuture != null && !HttpHeaders.isKeepAlive(request)) {
			writeFuture.addListener(ChannelFutureListener.CLOSE);
		}

		// Keep alive, need to tell channel handler it can return us to the pool
		if (HttpHeaders.isKeepAlive(request)) {
			keepaliveHelper.requestComplete(context);
		}

		return writeFuture;

	}

	private void checkFinished() {
		if (finished) {
			throw new IllegalStateException(
					"ServerResponse has already finished");
		}
	}

	@Override
	public boolean isFinished() {
		return finished;
	}

	@Override
	public void flush() throws IOException {
		writer.flush();
		out.flush();
	}

	/**
	 * Closes this request to future interaction.
	 */
	void close() {

		finished = true;

	}

	/**
	 * Writes messages as HttpChunk objects to the client.
	 */
	private class HttpChunkOutputStream extends OutputStream {

		private final ByteBuf content = Unpooled.buffer();
		private final ChannelHandlerContext context;
		private long writtenBytes = 0;

		HttpChunkOutputStream(final ChannelHandlerContext context_) {
			context = context_;
		}

		/**
		 * Adds a single byte to the output buffer.
		 */
		@Override
		public void write(final int b) throws IOException {
			content.writeByte(b);
			writtenBytes++;
		}

		public long writtenBytes() {
			return writtenBytes;
		}

		@Override
		public void flush() {

			if (!started) {
				startResponse();
			}

			final HttpContent chunk = new DefaultHttpContent(content);
			context.writeAndFlush(chunk);
		}

	}

}
