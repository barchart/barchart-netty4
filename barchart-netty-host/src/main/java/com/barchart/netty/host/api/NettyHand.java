package com.barchart.netty.host.api;

import io.netty.channel.ChannelOperationHandler;
import io.netty.channel.ChannelStateHandler;

/** represents individual netty handler */
public interface NettyHand extends NettyAny, ChannelStateHandler,
		ChannelOperationHandler {

}
