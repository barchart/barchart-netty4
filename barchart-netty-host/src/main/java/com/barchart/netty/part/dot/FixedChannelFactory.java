package com.barchart.netty.part.dot;

import io.netty.bootstrap.AbstractBootstrap.ChannelFactory;
import io.netty.channel.Channel;

public class FixedChannelFactory implements ChannelFactory {

	private final Channel channel;

	public FixedChannelFactory(final Channel channel) {
		this.channel = channel;
	}

	@Override
	public Channel newChannel() {
		return channel;
	}

}
