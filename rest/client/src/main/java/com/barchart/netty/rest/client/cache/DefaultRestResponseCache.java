package com.barchart.netty.rest.client.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;

import com.barchart.netty.rest.client.RestEndpoint;
import com.barchart.netty.rest.client.RestRequest;
import com.barchart.netty.rest.client.RestResponse;
import com.barchart.netty.rest.client.RestResponseCache;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * A response caching provider that allows specifying different caching rules based on path prefixes.
 */
public class DefaultRestResponseCache implements RestResponseCache {

	private final Map<RestEndpoint, Cache<Object, Object>> caches =
			new ConcurrentSkipListMap<RestEndpoint, Cache<Object, Object>>();

	@Override
	public <T> Observable<RestResponse<T>> intercept(final RestRequest request, final Observable<RestResponse<T>> remote) {

		for (final Map.Entry<RestEndpoint, Cache<Object, Object>> entry : caches.entrySet()) {

			if (entry.getKey().match(request.url().getPath())) {

				final Cache<Object, Object> cache = entry.getValue();

				@SuppressWarnings("unchecked")
				final RestResponse<T> response = (RestResponse<T>) cache.getIfPresent(entry.getKey());

				if (response != null) {
					return Observable.just(response);
				}

				return remote.doOnNext(new Action1<RestResponse<T>>() {

					@Override
					public void call(final RestResponse<T> r) {
						cache.put(request, r);
					}

				});

			}

		}

		return remote;

	}

	@Override
	public void clear() {
		for (final Cache<Object, Object> cache : caches.values()) {
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

		caches.put(new RestEndpoint(path), cb.build());

	}

}
