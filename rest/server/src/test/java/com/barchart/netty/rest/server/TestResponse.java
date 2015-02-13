/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.rest.server;

import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

import com.barchart.netty.server.http.request.HttpServerResponse;

public class TestResponse implements HttpServerResponse {

	public HttpResponseStatus status = HttpResponseStatus.OK;
	public DefaultHttpHeaders headers = new DefaultHttpHeaders();

	@Override
	public HttpHeaders headers() {
		return headers;
	}

	@Override
	public HttpVersion getProtocolVersion() {
		return null;
	}

	@Override
	public DecoderResult getDecoderResult() {
		return null;
	}

	@Override
	public void setDecoderResult(final DecoderResult result) {

	}

	@Override
	public HttpResponseStatus getStatus() {
		return status;
	}

	@Override
	public HttpServerResponse setStatus(final HttpResponseStatus status) {
		this.status = status;
		return null;
	}

	@Override
	public HttpServerResponse setProtocolVersion(final HttpVersion version) {
		return null;
	}

	@Override
	public int getChunkSize() {
		return 0;
	}

	@Override
	public void setChunkSize(final int chunkSize) {

	}

	@Override
	public void setCookie(final Cookie cookie) {

	}

	@Override
	public void setCookie(final String name, final String value) {

	}

	@Override
	public void setCharacterEncoding(final String charSet) {

	}

	@Override
	public Charset getCharacterEncoding() {
		return null;
	}

	@Override
	public void setContentLength(final int length) {

	}

	@Override
	public void setContentType(final String mimeType) {

	}

	@Override
	public void sendRedirect(final String location) {

	}

	@Override
	public OutputStream getOutputStream() {
		return null;
	}

	@Override
	public Writer getWriter() {
		return null;
	}

	@Override
	public void write(final String data) throws IOException {

	}

	@Override
	public void write(final byte[] data) throws IOException {

	}

	@Override
	public void write(final byte[] data, final int offset, final int length)
			throws IOException {

	}

	@Override
	public long writtenBytes() {
		return 0;
	}

	@Override
	public void flush() throws IOException {

	}

	@Override
	public ChannelFuture finish() throws IOException {
		return null;
	}

	@Override
	public boolean isFinished() {
		return false;
	}

	@Override
	public void fail(final HttpResponseStatus status, final Throwable t) {}

}