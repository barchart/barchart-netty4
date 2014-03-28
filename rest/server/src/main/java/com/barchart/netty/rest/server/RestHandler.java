/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.rest.server;

import java.io.IOException;

import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.netty.server.http.request.RequestHandler;

/**
 * Base handler for REST request processing. Subclasses should override the HTTP
 * methods that they support (get(), post(), put(), delete()).
 */
public interface RestHandler extends RequestHandler {

	/**
	 * Handle a GET request.
	 */
	void get(final HttpServerRequest request) throws IOException;

	/**
	 * Handle a POST request.
	 */
	void post(final HttpServerRequest request) throws IOException;

	/**
	 * Handle a PUT request.
	 */
	void put(final HttpServerRequest request) throws IOException;

	/**
	 * Handle a DELETE request.
	 */
	void delete(final HttpServerRequest request) throws IOException;

}
