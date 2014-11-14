/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.logging;

import com.barchart.netty.server.http.request.HttpServerRequest;

/**
 * HTTP request logging API.
 */
public interface RequestLogger {

	/**
	 * Log a completed request.
	 */
	public void access(HttpServerRequest request, long duration);

	/**
	 * Log a failed request.
	 */
	public void error(HttpServerRequest request, Throwable exception);

}
