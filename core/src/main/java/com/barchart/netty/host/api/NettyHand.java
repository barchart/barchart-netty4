package com.barchart.netty.host.api;

import io.netty.channel.ChannelOperationHandler;
import io.netty.channel.ChannelStateHandler;
import aQute.bnd.annotation.ProviderType;

/** represents individual netty handler */
@ProviderType
public interface NettyHand extends NettyAny, ChannelStateHandler,
		ChannelOperationHandler {

}
