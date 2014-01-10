/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.request;

import com.barchart.netty.server.HandlerFactory;

/**
 * Convenience tuple for returning the results of a handler / path prefix
 * lookup.
 */
public class HandlerMapping<H> {

	private final String path;
	private final HandlerFactory<H> factory;

	public HandlerMapping(final String path_, final HandlerFactory<H> factory_) {
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
	public HandlerFactory<H> factory() {
		return factory;
	}

}
