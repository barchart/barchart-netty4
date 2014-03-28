/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server;

/**
 * Factory for creating request handler instances, which allows more control
 * over the handler lifecycle.
 */
public interface HandlerFactory<H> {

	/**
	 * Create a new request handler.
	 */
	H newHandler();

}
