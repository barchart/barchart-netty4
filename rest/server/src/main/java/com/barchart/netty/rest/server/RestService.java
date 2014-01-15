package com.barchart.netty.rest.server;

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

	void add(final String pattern, final RequestHandler handler);

	void remove(final String pattern);

}
