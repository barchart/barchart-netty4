/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.api;

import aQute.bnd.annotation.ProviderType;

import com.barchart.osgi.factory.api.CidgetManager;
import com.typesafe.config.Config;

/**
 * Netty dot (end point) factory manager
 * <p>
 * FIXME change api.
 */
@ProviderType
public interface NettyDotManager extends CidgetManager<NettyDot> {

	/**
	 * Create a new dot form a properly formed hocon end point config entry
	 * <p>
	 * See net-point reference.conf for entry format
	 */
	NettyDot create(Config config);

	/**
	 * 
	 */
	boolean destroy(Config config);

	/**
	 * 
	 */
	boolean update(Config config);

}
