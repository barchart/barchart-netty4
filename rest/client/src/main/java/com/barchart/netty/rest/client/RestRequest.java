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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Representation of a REST request
 *
 * @param <T> The body data type
 */
public class RestRequest {

	public static enum Method {
		GET, POST, PUT, DELETE
	};

	private static Set<String> UNCACHED_HEADERS = new HashSet<String>() {
		{
			add("DATE");
			add("AUTHORIZATION");
		}
	};

	private final Map<String, List<String>> params;
	private final Map<String, List<String>> headers;
	private final Map<String, List<String>> cacheableHeaders;

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
		cacheableHeaders = new HashMap<String, List<String>>();

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
	public RestRequest param(final String param, final String value) {

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
	public RestRequest header(final String header, final String value) {

		List<String> values = headers.get(header);

		if (values == null) {
			values = new ArrayList<String>();
			headers.put(header, values);
		}

		values.add(value);

		if (!UNCACHED_HEADERS.contains(header.toUpperCase())) {
			cacheableHeaders.put(header, values);
		}

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
	public RestRequest data(final byte[] data_) {
		data = data_;
		return this;
	}

	/**
	 * Get the request body data.
	 */
	public byte[] data() {
		return data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + ((cacheableHeaders == null) ? 0 : cacheableHeaders.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final RestRequest other = (RestRequest) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		if (cacheableHeaders == null) {
			if (other.cacheableHeaders != null)
				return false;
		} else if (!cacheableHeaders.equals(other.cacheableHeaders))
			return false;
		if (method != other.method)
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RestRequest [method=" + method + ", url=" + url + ", params=" + params + ", headers=" + headers
				+ ", data=" + Arrays.toString(data) + "]";
	}

}