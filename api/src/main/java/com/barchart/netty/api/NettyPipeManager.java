/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.api;

import java.util.concurrent.TimeUnit;

import aQute.bnd.annotation.ProviderType;

/**
 * netty pipeline factory manager;
 * 
 * maintains registry of all present pipelines
 */
@ProviderType
public interface NettyPipeManager {

	/** @return valid pipe or null when not present */
	NettyPipe findPipe(String pipeName);

	/** @return valid pipe or null when not present */
	NettyPipe findPipe(String pipeName, long timeout, TimeUnit unit)
			throws InterruptedException;

}
