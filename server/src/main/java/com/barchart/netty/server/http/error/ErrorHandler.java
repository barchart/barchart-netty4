/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.error;

import java.io.IOException;

import aQute.bnd.annotation.ConsumerType;

import com.barchart.netty.server.http.request.ServerRequest;
import com.barchart.netty.server.http.request.ServerResponse;

/**
 * Error handler for failed requests.
 */
@ConsumerType
public interface ErrorHandler {

	/**
	 * Called when an error occurs during a request.
	 */
	public void onError(final ServerRequest request,
			final ServerResponse response, Throwable cause) throws IOException;

}
