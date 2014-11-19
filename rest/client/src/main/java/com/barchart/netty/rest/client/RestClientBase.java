/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.rest.client;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;

import com.barchart.netty.rest.client.RestRequest.Method;
import com.barchart.netty.rest.client.cache.DefaultRestResponseCache;
import com.barchart.netty.rest.client.transport.URLConnectionTransport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Base REST client which hides all HTTP implementation details from the end
 * user and provides helper functions for subclasses to implement service
 * proxies.
 *
 * @author jeremy
 */
public abstract class RestClientBase implements RestClient {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected final ObjectMapper mapper;

	private final SimpleModule module;
	private final RestTransport transport;
	private final String baseUrl;

	private Credentials credentials = null;
	private DefaultRestResponseCache cache = null;

	public RestClientBase(final String baseUrl_) {
		this(baseUrl_, new URLConnectionTransport());
	}

	public RestClientBase(final String baseUrl_, final RestTransport transport_) {

		mapper = new ObjectMapper();
		module = new SimpleModule("json", Version.unknownVersion());

		transport = transport_;
		baseUrl = baseUrl_;

	}

	/**
	 * The authentication credentials. This will be set automatically after a
	 * successful authenticate() response.
	 */
	@Override
	public Credentials credentials() {
		return credentials;
	}

	/**
	 * Set the authentication credentials for future requests.
	 */
	@Override
	public void credentials(final Credentials credentials_) {

		credentials = credentials_;

		if (cache != null)
			cache.clear();

	}

	/**
	 * Set the response cache for a path, using the same cache rules for successful responses and failures. The path
	 * specified can contain {placeholder} strings. @see RestEndpoint
	 */
	public void cache(final String path_, final int size, final long expiration, final TimeUnit units) {
		cache(path_, size, expiration, units, 0, 0, null);
	}

	/**
	 * Set the response cache for a path, using separate rules for successful responses and failures. The path specified
	 * can contain {placeholder} strings. @see RestEndpoint
	 */
	public void cache(final String path_,
			final int successSize, final long successExpiration, final TimeUnit successUnits,
			final int failSize, final long failExpiration, final TimeUnit failUnits) {

		if (cache == null)
			cache = new DefaultRestResponseCache();

		if (successSize > 0)
			cache.cache(baseUrl + path_, successSize, successExpiration, successUnits);

		if (failSize > 0)
			cache.failure(baseUrl + path_, failSize, failExpiration, failUnits);

	}

	/**
	 * The response cache.
	 */
	protected RestResponseCache cache() {
		return cache;
	}

	public boolean authenticated() {
		return credentials != null;
	}

	public void deauthorize() {
		credentials = null;
	}

	protected <T> void mapAbstractType(final Class<T> superType,
			final Class<? extends T> subType) {

		module.addAbstractTypeMapping(superType, subType);

		// Apparently we need to re-register this after every mapping
		mapper.registerModule(module);

	}

	protected RestRequest authenticate(final RestRequest request) {

		if (request.method() == Method.POST
				&& !request.headers().containsKey("Content-Type")) {
			request.header("Content-Type", "application/x-www-form-urlencoded");
		}

		if (credentials != null) {
			credentials.authenticate(request);
		}

		return request;

	}

	/**
	 * Send a request with no response processing.
	 */
	protected Observable<RestResponse<byte[]>> send(final RestRequest request) {

		final Observable<RestResponse<byte[]>> response = transport.send(authenticate(request));

		if (cache != null) {
			return cache.intercept(request, response);
		}

		return response;

	}

	/**
	 * Send a request, decoding the response body from JSON to the specified
	 * type.
	 */
	protected <T> Observable<RestResponse<T>> send(
			final RestRequest request, final Class<? extends T> responseType) {

		final Observable<RestResponse<T>> response = transport.send(authenticate(request)).map(
				new ResponseDecoder<T>() {

					@Override
					public final T decode(final byte[] content) throws Exception {
						return mapper.readValue(content, responseType);
					}

				});

		if (cache != null) {
			return cache.intercept(request, response);
		}

		return response;

	}

	/**
	 * Send a request, decoding the response body from JSON to the specified
	 * type reference.
	 */
	protected <T> Observable<RestResponse<T>> send(
			final RestRequest request,
			final TypeReference<? extends T> responseType) {

		final Observable<RestResponse<T>> response = transport.send(authenticate(request)).map(
				new ResponseDecoder<T>() {

					@Override
					public final T decode(final byte[] content)
							throws Exception {
						return mapper.readValue(content, responseType);
					}

				});

		return response;

	}

	/**
	 * Create a new request.
	 */
	protected RestRequest request(final Method method_, final String path_) {

		final RestRequest request = new RestRequest(method_, baseUrl + path_);

		// Manually set date for HMAC signing
		request.header("Date", httpDate(new Date()));

		return request;

	}

	/**
	 * Create a new request, with the specified content object encoded as JSON
	 * in the request body.
	 */
	protected <T> RestRequest request(final Method method_,
			final String path_, final T content_) {

		try {

			final JsonRestRequest<T> request =
					new JsonRestRequest<T>(method_, baseUrl + path_);

			// Manually set date for HMAC signing
			request.header("Date", httpDate(new Date()));

			return request.attach(content_);

		} catch (final MalformedURLException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Get a date header string that is in sync with the server.
	 */
	private String httpDate(final Date date) {
		// TODO account for client/service clock skew
		final SimpleDateFormat dateFormat =
				new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(date);
	}

	/**
	 * JSON request that handles encoding an attachment as JSON in the request
	 * body.
	 *
	 * @param <T> The object type
	 */
	protected class JsonRestRequest<T> extends RestRequest {

		public JsonRestRequest(final Method method_, final String url_)
				throws MalformedURLException {
			super(method_, url_);
		}

		/**
		 * Attach the specified object as a JSON-encoded request body.
		 * Automatically sets the Content-Type header to "application/json".
		 */
		public RestRequest attach(final T object_) {
			try {
				data(mapper.writeValueAsBytes(object_));
				header("Content-Type", "application/json");
				return this;
			} catch (final JsonProcessingException e) {
				throw new IllegalArgumentException(e);
			}
		}

	}

	/**
	 * A chain observer for decoding the binary JSON response from the transport
	 * into an object.
	 *
	 * @param <T> The object type
	 */
	protected abstract class ResponseDecoder<T> implements Func1<RestResponse<byte[]>, RestResponse<T>> {

		public abstract T decode(byte[] content) throws Exception;

		@Override
		public RestResponse<T> call(final RestResponse<byte[]> response) {

			return new RestResponse<T>() {

				@Override
				public Map<String, List<String>> headers() {
					return response.headers();
				}

				@Override
				public T content() {
					try {
						return decode(response.content());
					} catch (final Exception e) {
						log.warn("Could not decode response", e);
						if (response.content().length < 1024) {
							log.debug("Response: " + new String(response.content()));
						}
						return null;
					}
				}

				@Override
				public boolean success() {
					return response.success();
				}

				@Override
				public int status() {
					return response.status();
				}

				@Override
				public String error() {
					return response.error();
				}

			};

		}

	}

	/**
	 * A chain observer for transforming the response content from one type to
	 * another before passing on to the client observer.
	 *
	 * @param <S> The response content type
	 * @param <T> The transformed object type
	 */
	protected abstract class ResponseTransformer<S, T> implements Func1<RestResponse<S>, Observable<T>> {

		protected abstract Observable<T> transform(S content);

		@Override
		public Observable<T> call(final RestResponse<S> response) {
			if (response.success()) {
				try {
					return transform(response.content());
				} catch (final Throwable t) {
					return Observable.error(t);
				}
			} else {
				return Observable.error(new Exception(response.error()));
			}
		}

	}

	/**
	 * A chain observer for unwrapping the RestResponse content and passing it
	 * to an observer.
	 *
	 * @param <T> The response content type
	 */
	protected class ResponseUnwrapper<T, S extends T> extends
			ResponseTransformer<S, T> {

		public ResponseUnwrapper() {
		}

		@Override
		protected Observable<T> transform(final S content) {
			return Observable.<T> from(content);
		}

	}

	/**
	 * A chain observer for exploding a list of objects into separate
	 * notifications.
	 *
	 * @param <T> The object type
	 */
	protected class ListUnwrapper<T, S extends T> extends ResponseTransformer<List<S>, T> {

		public ListUnwrapper() {
		}

		@Override
		protected Observable<T> transform(final List<S> content) {
			return Observable.<T> from(content);
		}

	}

	protected static <T> Func1<RestResponse<?>, Observable<T>> replace(final T value) {

		return new Func1<RestResponse<?>, Observable<T>>() {
			@Override
			public final Observable<T> call(final RestResponse<?> response) {
				if (response.success()) {
					return Observable.from(value);
				} else {
					return Observable.error(new Exception(response.error()));
				}
			}
		};

	}

	protected static <T> Func1<RestResponse<?>, Observable<T>> replace(
			final List<T> value) {

		return new Func1<RestResponse<?>, Observable<T>>() {
			@Override
			public final Observable<T> call(final RestResponse<?> response) {
				if (response.success()) {
					return Observable.from(value);
				} else {
					return Observable.error(new Exception(response.error()));
				}
			}
		};

	}

	protected static <T> Func1<RestResponse<?>, Observable<T>> replace(
			final T[] value) {

		return new Func1<RestResponse<?>, Observable<T>>() {
			@Override
			public final Observable<T> call(final RestResponse<?> response) {
				if (response.success()) {
					return Observable.from(value);
				} else {
					return Observable.error(new Exception(response.error()));
				}
			}
		};

	}

	protected static <T> Func1<RestResponse<?>, Observable<T>> empty(
			final Class<T> cls) {

		return new Func1<RestResponse<?>, Observable<T>>() {
			@Override
			public final Observable<T> call(final RestResponse<?> response) {
				return Observable.empty();
			}
		};

	}

	/**
	 * Auto subscribe to an observable, caching the result for future
	 * subscriptions.
	 */
	protected static <T> Observable<T> cache(final Observable<T> observable) {

		final Observable<T> cached = observable.cache();

		cached.subscribe(new Observer<T>() {

			@Override
			public void onCompleted() {
			}

			@Override
			public void onError(final Throwable e) {
			}

			@Override
			public void onNext(final T args) {
			}

		});

		return cached;

	}

}
