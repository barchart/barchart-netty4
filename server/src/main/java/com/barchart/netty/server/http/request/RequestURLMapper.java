/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.request;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface RequestURLMapper {

	/**
	 * Find the correct request handler for the given request URI.
	 */
	RequestHandlerMapping getHandlerFor(String uri);

}
