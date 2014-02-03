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

import org.apache.commons.io.IOUtils;

import rx.Observable;
import rx.Observer;
import rx.Subscription;

import com.barchart.netty.rest.client.RestRequest;
import com.barchart.netty.rest.client.RestResponse;
import com.barchart.netty.rest.client.RestTransport;
import com.barchart.netty.rest.client.RestRequest.Method;

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
	public Observable<RestResponse<byte[]>> send(final RestRequest<?> request) {

		return Observable
				.create(new Observable.OnSubscribeFunc<RestResponse<byte[]>>() {

					@Override
					public Subscription onSubscribe(
							final Observer<? super RestResponse<byte[]>> observer) {

						if (executor == null) {
							runnable(request, observer).run();
						} else {
							executor.execute(runnable(request, observer));
						}

						return new Subscription() {
							@Override
							public void unsubscribe() {
							}
						};

					}

				});

	}

	public Runnable runnable(final RestRequest<?> request,
			final Observer<? super RestResponse<byte[]>> observer) {

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

					final HttpURLConnection conn =
							(HttpURLConnection) url.openConnection();
					conn.setRequestMethod(request.method().name());

					// Set default timeout parameters
					conn.setConnectTimeout(10000);
					conn.setReadTimeout(60000);

					boolean setContentType = false;

					for (final Map.Entry<String, List<String>> entry : request
							.headers().entrySet()) {

						if (entry.getKey().equalsIgnoreCase("Content-Type")) {
							setContentType = true;
						}

						for (final String value : entry.getValue()) {
							conn.setRequestProperty(entry.getKey(), value);
						}

					}

					if (request.data() == null
							&& request.method() == Method.POST
							&& request.params().size() > 0) {

						if (!setContentType) {
							conn.setRequestProperty("Content-Type",
									"application/x-www-form-urlencoded");
						}

						final StringBuilder sb = new StringBuilder();

						String join = "";

						for (final Map.Entry<String, List<String>> entry : request
								.params().entrySet()) {

							sb.append(join);

							for (final String value : entry.getValue()) {
								sb.append(
										URLEncoder.encode(entry.getKey(),
												"UTF-8"))
										.append('=')
										.append(URLEncoder.encode(value,
												"UTF-8"));
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

					observer.onNext(new URLConnectionResponse(conn));

					observer.onCompleted();

				} catch (final IOException ioe) {

					observer.onError(ioe);

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
				content = IOUtils.toByteArray(in);
			} else {
				content = new byte[] {};
			}
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
