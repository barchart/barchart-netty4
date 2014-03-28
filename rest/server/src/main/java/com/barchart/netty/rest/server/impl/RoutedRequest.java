/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.rest.server.impl;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.netty.server.http.request.HttpServerResponse;
import com.barchart.netty.server.http.request.RequestAttribute;
import com.barchart.netty.server.http.request.RequestAttributeKey;

public class RoutedRequest implements HttpServerRequest {

	private final HttpServerRequest request;
	private final String uri;
	private final Map<String, String> uriParams;
	private String remoteUser = null;

	private Map<String, List<String>> mergedParams = null;

	public RoutedRequest(final HttpServerRequest request_, final String uri_) {
		this(request_, uri_, null);
	}

	public RoutedRequest(final HttpServerRequest request_, final String uri_,
			final Map<String, String> params_) {
		request = request_;
		uri = uri_;
		uriParams = params_;
	}

	public HttpServerRequest delegate() {
		return request;
	}

	@Override
	public String getPathInfo() {
		if (uri != null) {
			return request.getPathInfo().substring(uri.length());
		}
		return request.getPathInfo();
	}

	public void setRemoteUser(final String user_) {
		remoteUser = user_;
	}

	@SuppressWarnings("serial")
	@Override
	public Map<String, List<String>> getParameters() {

		if (mergedParams == null) {

			if (uriParams != null) {

				final HashMap<String, List<String>> rp = new HashMap<String, List<String>>(request.getParameters());

				for (final Map.Entry<String, String> entry : uriParams.entrySet()) {
					if (rp.containsKey(entry.getKey())) {
						rp.get(entry.getKey()).add(entry.getValue());
					} else {
						rp.put(entry.getKey(), new ArrayList<String>() {
							{
								add(entry.getValue());
							}
						});
					}
				}

				mergedParams = Collections.unmodifiableMap(rp);

			} else {

				mergedParams = request.getParameters();

			}

		}

		return mergedParams;

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
	public String getRemoteUser() {
		if (remoteUser != null) {
			return remoteUser;
		}
		return request.getRemoteUser();
	}

	// Pure delegation methods

	@Override
	public HttpHeaders headers() {
		return request.headers();
	}

	@Override
	public HttpVersion getProtocolVersion() {
		return request.getProtocolVersion();
	}

	@Override
	public DecoderResult getDecoderResult() {
		return request.getDecoderResult();
	}

	@Override
	public HttpMethod getMethod() {
		return request.getMethod();
	}

	@Override
	public void setDecoderResult(final DecoderResult result) {
		request.setDecoderResult(result);
	}

	@Override
	public HttpServerRequest setProtocolVersion(final HttpVersion version) {
		return request.setProtocolVersion(version);
	}

	@Override
	public HttpServerRequest setMethod(final HttpMethod method) {
		return request.setMethod(method);
	}

	@Override
	public HttpServerRequest setUri(final String uri) {
		return request.setUri(uri);
	}

	@Override
	public boolean isChunkedEncoding() {
		return request.isChunkedEncoding();
	}

	@Override
	public String getUri() {
		return request.getUri();
	}

	@Override
	public String getHandlerUri() {
		return request.getHandlerUri();
	}

	@Override
	public String getQueryString() {
		return request.getQueryString();
	}

	@Override
	public String getScheme() {
		return request.getScheme();
	}

	@Override
	public String getServerHost() {
		return request.getServerHost();
	}

	@Override
	public InetSocketAddress getServerAddress() {
		return request.getServerAddress();
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return request.getRemoteAddress();
	}

	@Override
	public boolean isSecure() {
		return request.isSecure();
	}

	@Override
	public Map<String, Cookie> getCookies() {
		return request.getCookies();
	}

	@Override
	public Cookie getCookie(final String name) {
		return request.getCookie(name);
	}

	@Override
	public Charset getCharacterEncoding() {
		return request.getCharacterEncoding();
	}

	@Override
	public ByteBuf getContent() {
		return request.getContent();
	}

	@Override
	public String getContentType() {
		return request.getContentType();
	}

	@Override
	public long getContentLength() {
		return request.getContentLength();
	}

	@Override
	public InputStream getInputStream() {
		return request.getInputStream();
	}

	@Override
	public BufferedReader getReader() {
		return request.getReader();
	}

	@Override
	public <T> RequestAttribute<T> attr(final RequestAttributeKey<T> key) {
		return request.attr(key);
	}

	@Override
	public HttpServerResponse response() {
		return request.response();
	}

}
