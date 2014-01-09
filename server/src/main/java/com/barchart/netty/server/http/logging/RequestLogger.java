/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.logging;

import aQute.bnd.annotation.ConsumerType;

import com.barchart.netty.server.http.request.ServerRequest;
import com.barchart.netty.server.http.request.ServerResponse;

/**
 * HTTP request logging API.
 */
@ConsumerType
public interface RequestLogger {

	/**
	 * Log a completed request.
	 */
	public void access(ServerRequest request, ServerResponse response,
			long duration);

	/**
	 * Log a failed request.
	 */
	public void error(ServerRequest request, ServerResponse response,
			Throwable exception);

}
