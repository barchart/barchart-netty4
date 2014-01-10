/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.request;

import java.io.IOException;

import com.barchart.netty.server.Handler;

/**
 * Inbound request handler.
 */
public interface RequestHandler extends Handler<HttpServerRequest> {

	@Override
	void handle(HttpServerRequest request) throws IOException;

	@Override
	void cancel(HttpServerRequest request);

	@Override
	void release(HttpServerRequest request);

}
