/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server;

import java.io.IOException;

/**
 * Inbound request handler.
 */
public interface Handler<T> {

	/**
	 * Handle a new request.
	 */
	void handle(T request) throws IOException;

	/**
	 * Client disconnected before the response is completed. If there are any
	 * long-running asynchronous tasks running as a result of the request, you
	 * should cancel them.
	 */
	void cancel(T request);

	/**
	 * Called when the current request is completed. This method will always be
	 * called, even if the request is cancelled, and should not be taken as an
	 * indication of request success or failure.
	 */
	void release(T request);

}
