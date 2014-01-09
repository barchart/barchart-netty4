/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.error;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.server.http.request.ServerRequest;
import com.barchart.netty.server.http.request.ServerResponse;

/**
 * Very basic default error handler.
 */
public class DefaultErrorHandler implements ErrorHandler {

	private final static Logger log = LoggerFactory
			.getLogger(DefaultErrorHandler.class);

	@Override
	public void onError(final ServerRequest request,
			final ServerResponse response, final Throwable cause)
			throws IOException {

		if (cause != null) {

			log.warn("Uncaught exception thrown in request", cause);
			response.write(cause.getClass()
					+ " was thrown while processing this request.  See logs for more details.");

		} else {

			response.write("Request could not be processed.  Status code: "
					+ response.getStatus());

		}

		response.finish();

	}

}
