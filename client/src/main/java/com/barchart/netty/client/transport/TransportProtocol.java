package com.barchart.netty.client.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;

import java.net.SocketAddress;

import com.barchart.netty.common.PipelineInitializer;

public interface TransportProtocol extends PipelineInitializer {

	public static final Bootstrap DEFAULT_BOOTSTRAP = new Bootstrap()
			.option(ChannelOption.SO_REUSEADDR, true)
			.option(ChannelOption.SO_SNDBUF, 262144)
			.option(ChannelOption.SO_RCVBUF, 262144);

	public enum Event {
		CONNECTED, DISCONNECTED
	}

	Class<? extends Channel> channel();

	SocketAddress address();

	Bootstrap bootstrap();

}