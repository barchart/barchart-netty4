package com.barchart.netty.api;

import io.netty.channel.ChannelOperationHandler;
import io.netty.channel.ChannelStateHandler;
import aQute.bnd.annotation.ProviderType;

/**
 * Individual netty handler.
 */
@ProviderType
public interface NettyHand extends NettyAny, ChannelStateHandler,
		ChannelOperationHandler {

}
