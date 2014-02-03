package com.barchart.netty.rest.client;

import rx.Observable;

/**
 * Transport implementation for REST requests.
 */
public interface RestTransport {

	/**
	 * Send a request to the remote server.
	 */
	Observable<RestResponse<byte[]>> send(final RestRequest<?> request);

}
