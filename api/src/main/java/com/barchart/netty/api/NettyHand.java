/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.api;

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import aQute.bnd.annotation.ProviderType;

/**
 * Individual netty handler.
 */
@ProviderType
public interface NettyHand extends NettyAny, ChannelInboundHandler,
		ChannelOutboundHandler {

}
