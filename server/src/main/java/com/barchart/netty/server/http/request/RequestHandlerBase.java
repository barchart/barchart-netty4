/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.request;


/**
 * Base request handler that provides default implementations of some less-used
 * methods.
 */
public abstract class RequestHandlerBase implements RequestHandler {

	@Override
	public void onAbort(final ServerRequest request,
			final ServerResponse response) {
	}

	@Override
	public void onException(final ServerRequest request,
			final ServerResponse response, final Throwable exception) {
	}

	@Override
	public void onComplete(final ServerRequest request,
			final ServerResponse response) {
	}

}
