/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.rest.client;

/**
 * Marker interface for REST clients.
 */
public interface RestClient {

	Credentials credentials();

	/**
	 * Set the authentication credentials for future requests.
	 */
	void credentials(final Credentials credentials);

}
