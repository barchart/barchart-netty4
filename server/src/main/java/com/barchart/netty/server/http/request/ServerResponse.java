/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.request;

import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

import aQute.bnd.annotation.ProviderType;

/**
 * Encapsulates a response to an inbound ServerRequest.
 */
@ProviderType
public interface ServerResponse extends ServerMessage, HttpResponse {

	@Override
	HttpResponseStatus getStatus();

	@Override
	ServerResponse setStatus(HttpResponseStatus status);

	@Override
	ServerResponse setProtocolVersion(HttpVersion version);

	boolean isChunkedEncoding();

	void setChunkedEncoding(final boolean chunked);

	/**
	 * Send a cookie to the client.
	 */
	void setCookie(Cookie cookie);

	/**
	 * Send a cookie to the client.
	 */
	void setCookie(String name, String value);

	/**
	 * Set the character encoding for this response (default is UTF-8).
	 */
	void setCharacterEncoding(String charSet);

	/**
	 * Set the character encoding for this response (default is UTF-8).
	 */
	Charset getCharacterEncoding();

	/**
	 * Set the content-length for this response. This is set automatically by
	 * default if chunked transfer encoding is not active.
	 */
	void setContentLength(int length);

	/**
	 * Set the response content MIME type.
	 */
	void setContentType(String mimeType);

	/**
	 * Send a URL redirect to the client.
	 */
	void sendRedirect(String location);

	/**
	 * Get the raw output stream for writing to the client. Note that unless
	 * chunked transfer encoding is turned on, all output will still be
	 * buffered.
	 */
	OutputStream getOutputStream();

	/**
	 * Get a writer that writes data directly to the client. Note that unless
	 * chunked transfer encoding is turned on, all output will still be
	 * buffered.
	 */
	Writer getWriter();

	/**
	 * Write a string to the client.
	 */
	void write(String data) throws IOException;

	/**
	 * Write a byte stream to the client.
	 */
	void write(byte[] data) throws IOException;

	/**
	 * Write a byte stream to the client.
	 */
	void write(byte[] data, int offset, int length) throws IOException;

	/**
	 * Get the number of bytes written to the client for this response.
	 */
	long writtenBytes();

	/**
	 * Flush the output buffers. Buffers are flushed automatically, and this
	 * should not usually be necessary.
	 */
	void flush() throws IOException;

	/**
	 * Complete this response, and release any resources associated with it.
	 * This must be called in every handler to avoid hung connections.
	 * 
	 * If a handler throws an unchecked exception, it will be finished
	 * automatically.
	 */
	ChannelFuture finish() throws IOException;

	/**
	 * Check if this response has been finished.
	 */
	boolean isFinished();

}
