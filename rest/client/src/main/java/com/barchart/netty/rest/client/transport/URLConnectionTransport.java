/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.rest.client.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import rx.Observable;
import rx.Subscriber;

import com.barchart.netty.rest.client.RestRequest;
import com.barchart.netty.rest.client.RestRequest.Method;
import com.barchart.netty.rest.client.RestResponse;
import com.barchart.netty.rest.client.RestTransport;
import com.google.common.io.ByteStreams;

public class URLConnectionTransport implements RestTransport {

	private final Executor executor;

	/**
	 * Default transport constructor that notifies observers synchronously.
	 */
	public URLConnectionTransport() {
		executor = null;
	}

	/**
	 * Execute requests with the given executor for asynchronous flow.
	 */
	public URLConnectionTransport(final Executor executor_) {
		executor = executor_;
	}

	@Override
	public <T> Observable<RestResponse<byte[]>> send(final RestRequest request) {

		return Observable.create(new Observable.OnSubscribe<RestResponse<byte[]>>() {

			@Override
			public void call(final Subscriber<? super RestResponse<byte[]>> subscriber) {

				if (executor == null) {
					runnable(request, subscriber).run();
				} else {
					executor.execute(runnable(request, subscriber));
				}

			}

		});

	}

	public Runnable runnable(final RestRequest request, final Subscriber<? super RestResponse<byte[]>> subscriber) {

		return new Runnable() {

			@Override
			public void run() {

				try {

					URL url;

					if ((request.method() == Method.GET || request.method() == Method.DELETE)) {
						url = request.urlWithQueryString();
					} else {
						url = request.url();
					}

					final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod(request.method().name());

					// Set default timeout parameters
					conn.setConnectTimeout(10000);
					conn.setReadTimeout(60000);

					boolean setContentType = false;

					for (final Map.Entry<String, List<String>> entry : request.headers().entrySet()) {

						if (entry.getKey().equalsIgnoreCase("Content-Type")) {
							setContentType = true;
						}

						for (final String value : entry.getValue()) {
							conn.setRequestProperty(entry.getKey(), value);
						}

					}

					if (request.data() == null && request.method() == Method.POST && request.params().size() > 0) {

						if (!setContentType) {
							conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
						}

						final StringBuilder sb = new StringBuilder();

						String join = "";

						for (final Map.Entry<String, List<String>> entry : request.params().entrySet()) {

							sb.append(join);

							for (final String value : entry.getValue()) {
								sb.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append('=')
										.append(URLEncoder.encode(value, "UTF-8"));
							}

							join = "&";

						}

						request.data(sb.toString().getBytes("UTF-8"));

					}

					if (request.data() != null) {
						conn.setDoOutput(true);
					}

					conn.connect();

					if (request.data() != null) {
						final OutputStream out = conn.getOutputStream();
						out.write(request.data());
						out.close();
					}

					subscriber.onNext(new URLConnectionResponse(conn));

					subscriber.onCompleted();

				} catch (final IOException ioe) {

					subscriber.onError(ioe);

				}

			}

		};

	}

	private static class URLConnectionResponse implements RestResponse<byte[]> {

		private final int status;
		private final Map<String, List<String>> headers;

		private byte[] content = null;

		URLConnectionResponse(final HttpURLConnection conn) throws IOException {
			status = conn.getResponseCode();
			headers = conn.getHeaderFields();
			final InputStream in =
					status < 400 ? conn.getInputStream() : conn
							.getErrorStream();
			if (in != null) {
				content = ByteStreams.toByteArray(in);
			} else {
				content = new byte[] {};
			}
			// TODO: configure keepalive handling
			// conn.disconnect();
		}

		@Override
		public Map<String, List<String>> headers() {
			return headers;
		}

		@Override
		public byte[] content() {
			return content;
		}

		@Override
		public boolean success() {
			return status < 400;
		}

		@Override
		public int status() {
			return status;
		}

		@Override
		public String error() {

			if (!success()) {
				try {
					return new String(content, "UTF-8");
				} catch (final UnsupportedEncodingException e) {
					return new String(content);
				}
			}

			return null;

		}

	}

}
