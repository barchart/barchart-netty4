/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.request;

import java.io.IOException;

import aQute.bnd.annotation.ConsumerType;

/**
 * Inbound request handler.
 */
@ConsumerType
public interface RequestHandler {

	/**
	 * Called when a new request is received from the client.
	 */
	void onRequest(ServerRequest request, ServerResponse response)
			throws IOException;

	/**
	 * Called when the request encounters an exception, either in the pipeline
	 * or as part of async processing.
	 */
	void onException(ServerRequest request, ServerResponse response,
			Throwable exception);

	/**
	 * Called when the client disconnects before the response is completed.
	 * Attempting to write to the response object after this method has been
	 * called will always throw exceptions.
	 */
	void onAbort(ServerRequest request, ServerResponse response);

	/**
	 * Called when the current request is completed. This method will always be
	 * called, even if the request is aborted or an exception is thrown, so
	 * should not be taken as an indication of request success or failure.
	 */
	void onComplete(ServerRequest request, ServerResponse response);

}
