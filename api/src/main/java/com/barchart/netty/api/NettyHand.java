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
