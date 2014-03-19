package com.barchart.netty.rest.server;

import com.barchart.netty.rest.client.RestEndpoint;
import com.barchart.netty.server.http.request.RequestHandler;

/**
 * Root module for REST services. Uses a Router internally for request
 * processing. Subclass to create independent OSGI-enabled service modules that
 * have multiple internal REST service endpoints.
 *
 * @author jeremy
 *
 */
public interface RestService extends RequestHandler {

	/**
	 * @see Router#add(RestEndpoint, RequestHandler)
	 */
	void add(final RestEndpoint endpoint, final RequestHandler handler);

	/**
	 * @see Router#remove(RestEndpoint)
	 */
	void remove(final RestEndpoint endpoint);

	/**
	 * @see Router#add(String, RequestHandler)
	 */
	void add(final String pattern, final RequestHandler handler);

	/**
	 * @see Router#remove(String)
	 */
	void remove(final String pattern);

}
