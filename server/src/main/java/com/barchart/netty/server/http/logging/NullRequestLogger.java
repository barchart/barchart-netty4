/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.logging;

import com.barchart.netty.server.http.request.ServerRequest;
import com.barchart.netty.server.http.request.ServerResponse;

public class NullRequestLogger implements RequestLogger {

	@Override
	public void access(final ServerRequest request,
			final ServerResponse response, final long duration) {
		// Noop
	}

	@Override
	public void error(final ServerRequest request,
			final ServerResponse response, final Throwable exception) {
		// Noop
	}

}
