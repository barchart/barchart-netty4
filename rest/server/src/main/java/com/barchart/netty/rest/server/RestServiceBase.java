/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.rest.server;

import java.io.IOException;

import com.barchart.netty.rest.client.RestEndpoint;
import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.netty.server.http.request.RequestHandler;
import com.barchart.netty.server.http.request.RequestHandlerBase;

/**
 * Root module for REST services. Uses a Router internally for request processing. Subclass to create independent
 * injectable service modules that have multiple internal REST service endpoints.
 *
 * @author jeremy
 *
 */
public class RestServiceBase extends RequestHandlerBase implements RestService {

	private final Router router = new Router();

	@Override
	public void add(final RestEndpoint endpoint, final RequestHandler handler) {
		router.add(endpoint, handler);
	}

	@Override
	public void remove(final RestEndpoint endpoint) {
		router.remove(endpoint);
	}

	@Override
	public void add(final String pattern, final RequestHandler handler) {
		router.add(pattern, handler);
	}

	@Override
	public void remove(final String pattern) {
		router.remove(pattern);
	}

	@Override
	public void handle(final HttpServerRequest request) throws IOException {
		router.handle(request);
	}

	@Override
	public void cancel(final HttpServerRequest request) {
		router.cancel(request);
	}

	@Override
	public void release(final HttpServerRequest request) {
		router.release(request);
	}

}
