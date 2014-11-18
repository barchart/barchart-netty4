package com.barchart.netty.rest.client;

import rx.Observable;

/**
 * REST response cache for performance.
 */
public interface RestResponseCache {

	/**
	 * Intercept a REST request. If a cached value is missing (or the cache doesn't care about caching this request), it
	 * must return the <i>remote</i> parameter as the method return value. Otherwise, <i>remote</i> can also be used to
	 * populate the cache on the return trip.
	 */
	<T> Observable<RestResponse<T>> intercept(RestRequest request, Observable<RestResponse<T>> remote);

	/**
	 * Clear all cached responses.
	 */
	void clear();

}