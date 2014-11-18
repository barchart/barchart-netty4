/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.rest.client;

import rx.Observable;

/**
 * Transport implementation for REST requests.
 */
public interface RestTransport {

	/**
	 * Send a request to the remote server.
	 */
	<T> Observable<RestResponse<byte[]>> send(final RestRequest request);

}
