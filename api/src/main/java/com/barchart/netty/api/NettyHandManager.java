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

/**
 * Handler factory manager.
 */
@ProviderType
public interface NettyHandManager extends CidgetManager<NettyHand> {

}
