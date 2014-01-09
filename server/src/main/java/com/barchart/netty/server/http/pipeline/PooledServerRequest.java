/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.barchart.netty.server.http.request.RequestAttribute;
import com.barchart.netty.server.http.request.RequestAttributeKey;
import com.barchart.netty.server.http.request.ServerRequest;

/**
 * Implements a server request from a low garbage collection use pool
 */
public class PooledServerRequest implements ServerRequest {

	private FullHttpRequest nettyRequest;

	private String baseUri;
	private String pathInfo;
	private String queryString;

	private InetSocketAddress local;
	private InetSocketAddress remote;

	private Map<String, List<String>> queryStringDecoded = null;
	private Map<String, Cookie> cookies;

	private Map<RequestAttributeKey<?>, RequestAttribute<?>> attributes;

	private String remoteUser = null;

	public PooledServerRequest() {
	}

	void init(final Channel channel_, final FullHttpRequest nettyRequest_,
			final String relativeUri_) {

		local = (InetSocketAddress) channel_.localAddress();
		remote = (InetSocketAddress) channel_.remoteAddress();

		nettyRequest = nettyRequest_;
		nettyRequest.retain();

		baseUri = relativeUri_;

		final int q = baseUri.indexOf('?');

		if (q == -1) {
			pathInfo = baseUri;
			queryString = null;
		} else {
			pathInfo = baseUri.substring(0, q);
			queryString = baseUri.substring(q + 1);
		}

		// Reset previous state
		queryStringDecoded = null;
		cookies = null;
		attributes = null;

		remoteUser = null;
	}

	void release() {
		if (nettyRequest != null) {
			nettyRequest.release();
		}
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	@Override
	public String getHandlerUri() {
		return baseUri;
	}

	@Override
	public String getPathInfo() {
		return pathInfo;
	}

	@Override
	public String getScheme() {
		return isSecure() ? "https" : "http";
	}

	@Override
	public String getServerHost() {
		return HttpHeaders.getHost(nettyRequest);
	}

	@Override
	public InetSocketAddress getServerAddress() {
		return local;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return remote;
	}

	@Override
	public boolean isSecure() {
		// No SSL support currently
		return false;
	}

	@Override
	public String getContentType() {
		return HttpHeaders.getHeader(nettyRequest,
				HttpHeaders.Names.CONTENT_TYPE);
	}

	@Override
	public Charset getCharacterEncoding() {

		final String contentType = getContentType();
		final int pos = contentType.indexOf(";");

		if (pos == -1) {
			return CharsetUtil.ISO_8859_1;
		}

		return Charset.forName(contentType.substring(pos + 1).trim());

	}

	@Override
	public long getContentLength() {
		return HttpHeaders.getContentLength(nettyRequest, 0);
	}

	@Override
	public InputStream getInputStream() {
		return new ByteBufInputStream(nettyRequest.content());
	}

	@Override
	public BufferedReader getReader() {
		return new BufferedReader(new InputStreamReader(getInputStream(),
				getCharacterEncoding()));
	}

	@Override
	public Map<String, List<String>> getParameters() {

		if (queryStringDecoded == null) {

			if (HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED
					.equals(headers().get(HttpHeaders.Names.CONTENT_TYPE))) {
				queryStringDecoded =
						new QueryStringDecoder(nettyRequest.content().toString(
								getCharacterEncoding()), false).parameters();
			} else if (queryString != null) {
				queryStringDecoded =
						new QueryStringDecoder(queryString, false).parameters();
			} else {
				queryStringDecoded = new HashMap<String, List<String>>();
			}

		}

		return queryStringDecoded;

	}

	@Override
	public String getParameter(final String name) {

		final Map<String, List<String>> params = getParameters();

		if (params != null) {

			final List<String> values = params.get(name);

			if (values != null && values.size() > 0) {
				return values.get(0);
			}

		}

		return null;

	}

	@Override
	public List<String> getParameterList(final String name) {

		final Map<String, List<String>> params = getParameters();

		if (params != null) {
			return params.get(name);
		}

		return null;
	}

	@Override
	public Map<String, Cookie> getCookies() {

		if (cookies == null) {

			cookies = new HashMap<String, Cookie>();

			final Set<Cookie> cookieSet =
					CookieDecoder.decode(nettyRequest.headers().get("Cookie"));

			for (final Cookie cookie : cookieSet) {
				cookies.put(cookie.getName(), cookie);
			}

		}

		return cookies;

	}

	@Override
	public Cookie getCookie(final String name) {

		final Map<String, Cookie> cookies = getCookies();

		if (cookies != null) {
			return cookies.get(name);
		}

		return null;
	}

	public void setRemoteUser(final String user) {
		remoteUser = user;
	}

	@Override
	public String getRemoteUser() {
		return remoteUser;
	}

	/*
	 * Delegate to FullHttpRequest
	 */

	@Override
	public HttpMethod getMethod() {
		return nettyRequest.getMethod();
	}

	@Override
	public ServerRequest setMethod(final HttpMethod method) {
		nettyRequest.setMethod(method);
		return this;
	}

	@Override
	public String getUri() {
		return nettyRequest.getUri();
	}

	@Override
	public ServerRequest setUri(final String uri) {
		nettyRequest.setUri(uri);
		return this;
	}

	@Override
	public HttpHeaders headers() {
		return nettyRequest.headers();
	}

	@Override
	public HttpVersion getProtocolVersion() {
		return nettyRequest.getProtocolVersion();
	}

	@Override
	public ServerRequest setProtocolVersion(final HttpVersion version) {
		nettyRequest.setProtocolVersion(version);
		return this;
	}

	@Override
	public ByteBuf getContent() {
		return nettyRequest.content();
	}

	@Override
	public boolean isChunkedEncoding() {
		return HttpHeaders.isTransferEncodingChunked(nettyRequest);
	}

	@Override
	public DecoderResult getDecoderResult() {
		return nettyRequest.getDecoderResult();
	}

	@Override
	public void setDecoderResult(final DecoderResult result) {
		nettyRequest.setDecoderResult(result);
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized <T> RequestAttribute<T> attr(
			final RequestAttributeKey<T> key) {

		if (attributes == null) {
			attributes =
					new HashMap<RequestAttributeKey<?>, RequestAttribute<?>>(2);
		}

		RequestAttribute<T> attr = (RequestAttribute<T>) attributes.get(key);
		if (attr == null) {
			attr = new RequestAttribute<T>();
			attributes.put(key, attr);
		}

		return attr;

	}

}
