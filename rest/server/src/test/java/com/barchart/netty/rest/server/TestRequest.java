package com.barchart.netty.rest.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.netty.server.http.request.HttpServerResponse;
import com.barchart.netty.server.http.request.RequestAttribute;
import com.barchart.netty.server.http.request.RequestAttributeKey;

public class TestRequest implements HttpServerRequest {

	private final HashMap<String, List<String>> params =
			new HashMap<String, List<String>>();

	private final HttpHeaders headers = new DefaultHttpHeaders();
	private final HttpServerResponse response = new TestResponse();

	private final String pathInfo;
	private final String uri;
	private final String user;
	public HttpMethod method = HttpMethod.GET;
	public ByteBuf content = Unpooled.buffer();

	public TestRequest(final String uri_, final String pathInfo_,
			final String user_) {
		uri = uri_;
		pathInfo = pathInfo_;
		user = user_;
	}

	@Override
	public String getHandlerUri() {
		return pathInfo;
	}

	@Override
	public String getPathInfo() {
		return pathInfo;
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public Map<String, List<String>> getParameters() {
		return params;
	}

	@Override
	public String getRemoteUser() {
		return user;
	}

	@Override
	public HttpMethod getMethod() {
		return method;
	}

	@Override
	public HttpHeaders headers() {
		return headers;
	}

	// No-ops

	@Override
	public HttpVersion getProtocolVersion() {
		return HttpVersion.HTTP_1_1;
	}

	@Override
	public DecoderResult getDecoderResult() {
		return null;
	}

	@Override
	public void setDecoderResult(final DecoderResult result) {
	}

	@Override
	public HttpServerRequest setProtocolVersion(final HttpVersion version) {
		return null;
	}

	@Override
	public HttpServerRequest setMethod(final HttpMethod method) {
		return null;
	}

	@Override
	public HttpServerRequest setUri(final String uri) {
		return null;
	}

	@Override
	public boolean isChunkedEncoding() {
		return false;
	}

	@Override
	public String getQueryString() {
		return null;
	}

	@Override
	public String getScheme() {
		return null;
	}

	@Override
	public String getServerHost() {
		return null;
	}

	@Override
	public InetSocketAddress getServerAddress() {
		return null;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return null;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public String getParameter(final String name) {
		return null;
	}

	@Override
	public List<String> getParameterList(final String name) {
		return null;
	}

	@Override
	public Map<String, Cookie> getCookies() {
		return null;
	}

	@Override
	public Cookie getCookie(final String name) {
		return null;
	}

	@Override
	public Charset getCharacterEncoding() {
		return null;
	}

	@Override
	public ByteBuf getContent() {
		return content;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public long getContentLength() {
		return 0;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public BufferedReader getReader() {
		return null;
	}

	@Override
	public <T> RequestAttribute<T> attr(final RequestAttributeKey<T> key) {
		return null;
	}

	@Override
	public HttpServerResponse response() {
		return response;
	}

}