/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.rest.server.filter;

import rx.Observable;

import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.netty.server.http.request.RequestHandler;

public interface RestAuthenticator extends RequestHandler {

	/**
	 * The authentication type (BASIC, etc)
	 */
	public String type();

	/**
	 * Attempt to authenticate the request and return the authorized user ID, or
	 * null if authentication failed.
	 */
	public Observable<String> authenticate(String token,
			HttpServerRequest request);

}
