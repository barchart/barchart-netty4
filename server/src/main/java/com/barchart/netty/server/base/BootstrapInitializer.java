/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.base;

import io.netty.bootstrap.AbstractBootstrap;

/**
 * Base interface for objects that are interested in configuring channel options
 * before the remote connect is initiated.
 */
public interface BootstrapInitializer<B extends AbstractBootstrap<B, ?>> {

	/**
	 * Initialize a Netty Bootstrap for additional flexibility in configuring
	 * channel options. You should generally only call options() on the provided
	 * ServerBootstrap, as other values (local host, channel type, channel
	 * initializer, etc) may be overwritten by the default bootstrapping
	 * process.
	 */
	public void initBootstrap(final B bootstrap);

}
