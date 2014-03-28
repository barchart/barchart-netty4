/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.rest.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representation of a REST request
 * 
 * @param <T> The body data type
 */
public class RestRequest<T> {

	public static enum Method {
		GET, POST, PUT, DELETE
	};

	private final Map<String, List<String>> params;
	private final Map<String, List<String>> headers;

	private byte[] data = null;

	private final Method method;
	private final URL url;

	/**
	 * Create a new request with the specified HTTP method and URL.
	 */
	public RestRequest(final Method method_, final String url_) {

		method = method_;
		try {
			url = new URL(url_);
		} catch (final MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}

		params = new HashMap<String, List<String>>();
		headers = new HashMap<String, List<String>>();

	}

	/**
	 * Get the request method.
	 */
	public Method method() {
		return method;
	}

	/**
	 * Get the requested URL.
	 */
	public URL url() {
		return url;
	}

	/**
	 * Get the requested URL with parameters appended.
	 */
	public URL urlWithQueryString() {

		// Construct URL with parameters

		if (params.size() > 0) {

			try {

				final StringBuilder sb = new StringBuilder(url().toString());

				char join = sb.indexOf("?") == -1 ? '?' : '&';

				for (final Map.Entry<String, List<String>> entry : params()
						.entrySet()) {

					sb.append(join);

					for (final String value : entry.getValue()) {
						sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
								.append('=')
								.append(URLEncoder.encode(value, "UTF-8"));
					}

					join = '&';

				}

				return new URL(sb.toString());

			} catch (final Throwable t) {

			}

		}

		return url();

	}

	/**
	 * Set a request parameter.
	 */
	public RestRequest<T> param(final String param, final String value) {

		List<String> values = params.get(param);

		if (values == null) {
			values = new ArrayList<String>();
			params.put(param, values);
		}

		values.add(value);

		return this;

	}

	/**
	 * Get all request parameters.
	 */
	public Map<String, List<String>> params() {
		return params;
	}

	/**
	 * Set a request header.
	 */
	public RestRequest<T> header(final String header, final String value) {

		List<String> values = headers.get(header);

		if (values == null) {
			values = new ArrayList<String>();
			headers.put(header, values);
		}

		values.add(value);

		return this;

	}

	/**
	 * Get all request headers.
	 */
	public Map<String, List<String>> headers() {
		return headers;
	}

	/**
	 * Set the request body data.
	 */
	public RestRequest<T> data(final byte[] data_) {
		data = data_;
		return this;
	}

	/**
	 * Get the request body data.
	 */
	public byte[] data() {
		return data;
	}

}