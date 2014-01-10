/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.request;

/**
 * Convenience tuple for returning the results of a RequestHandler / path prefix
 * lookup.
 */
public class RequestHandlerMapping {

	private final String path;
	private final RequestHandlerFactory factory;

	public RequestHandlerMapping(final String path_,
			final RequestHandlerFactory factory_) {
		path = path_;
		factory = factory_;
	}

	/**
	 * The configured prefix for this handler.
	 */
	public String path() {
		return path;
	}

	/**
	 * The request handler factory for this request.
	 */
	public RequestHandlerFactory factory() {
		return factory;
	}

}
