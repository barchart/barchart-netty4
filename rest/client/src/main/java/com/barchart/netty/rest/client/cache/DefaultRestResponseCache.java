package com.barchart.netty.rest.client.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;

import com.barchart.netty.rest.client.RestEndpoint;
import com.barchart.netty.rest.client.RestRequest;
import com.barchart.netty.rest.client.RestRequest.Method;
import com.barchart.netty.rest.client.RestResponse;
import com.barchart.netty.rest.client.RestResponseCache;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * A response caching provider that allows specifying different caching rules based on path prefixes.
 */
public class DefaultRestResponseCache implements RestResponseCache {

	final Map<RestEndpoint, Cache<Object, Object>> responses =
			new ConcurrentSkipListMap<RestEndpoint, Cache<Object, Object>>();

	final Map<RestEndpoint, Cache<Object, Object>> failures =
			new ConcurrentSkipListMap<RestEndpoint, Cache<Object, Object>>();

	@Override
	public <T> Observable<RestResponse<T>> intercept(final RestRequest request, final Observable<RestResponse<T>> remote) {

		Observable<RestResponse<T>> response;

		// Only cache GET requests
		if (request.method() != Method.GET)
			return remote;

		// Check for successful response cache, or passthrough and cache responses
		if ((response = tryCache(responses, request, remote, false)) != null)
			return response;

		// Check for failure response cache, or passthrough and cache failures
		if ((response = tryCache(failures, request, remote, true)) != null)
			return response;

		// Turns out we don't care about this request, pass through
		return remote;

	}

	protected Cache<Object, Object> find(final Map<RestEndpoint, Cache<Object, Object>> caches,
			final RestRequest request) {

		for (final Map.Entry<RestEndpoint, Cache<Object, Object>> entry : caches.entrySet()) {
			if (entry.getKey().match(request.url().getPath())) {
				return entry.getValue();
			}
		}

		return null;

	}

	protected <T> Observable<RestResponse<T>> tryCache(
			final Map<RestEndpoint, Cache<Object, Object>> caches,
			final RestRequest request,
			final Observable<RestResponse<T>> remote,
			final boolean failuresOnly) {

		final Cache<Object, Object> cache = find(caches, request);

		if (cache != null) {

			@SuppressWarnings("unchecked")
			final RestResponse<T> response = (RestResponse<T>) cache.getIfPresent(request);

			if (response != null) {
				return Observable.just(response);
			}

			return remote.doOnNext(new Action1<RestResponse<T>>() {

				@Override
				public void call(final RestResponse<T> r) {

					if (!r.success()) {

						final Cache<Object, Object> failCache =
								failuresOnly ? cache : find(failures, request);

						if (failCache != null) {
							failCache.put(request, r);
							return;
						}

					}

					if (!failuresOnly) {
						cache.put(request, r);
					}

				}

			});

		}

		return null;

	}

	@Override
	public void clear() {
		for (final Cache<Object, Object> cache : responses.values()) {
			cache.invalidateAll();
		}
	}

	/**
	 * Add a caching rule to a path prefix.
	 *
	 * @param path The path prefix to cache
	 * @param maxSize The maximum cache size for this path
	 * @param expires The expiration time (after initial write)
	 * @param units The expiration time units
	 */
	public void cache(final String path, final int maxSize, final long expires, final TimeUnit units) {

		final CacheBuilder<Object, Object> cb = CacheBuilder.newBuilder();

		if (maxSize > 0) {
			cb.maximumSize(maxSize);
		}

		if (expires > 0) {
			cb.expireAfterWrite(expires, units);
		}

		responses.put(new RestEndpoint(path), cb.build());

	}

	/**
	 * Add a caching rule to a path prefix that only caches failures.
	 *
	 * @param path The path prefix to cache
	 * @param maxSize The maximum cache size for this path
	 * @param expires The expiration time (after initial write)
	 * @param units The expiration time units
	 */
	public void failure(final String path, final int maxSize, final long expires, final TimeUnit units) {

		final CacheBuilder<Object, Object> cb = CacheBuilder.newBuilder();

		if (maxSize > 0) {
			cb.maximumSize(maxSize);
		}

		if (expires > 0) {
			cb.expireAfterWrite(expires, units);
		}

		failures.put(new RestEndpoint(path), cb.build());

	}

}
