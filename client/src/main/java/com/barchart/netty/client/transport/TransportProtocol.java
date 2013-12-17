package com.barchart.netty.client.transport;

import io.netty.channel.Channel;

import java.net.SocketAddress;

import com.barchart.netty.client.PipelineInitializer;

public interface TransportProtocol extends PipelineInitializer {

	public enum Event {
		CONNECTED, DISCONNECTED
	}

	Class<? extends Channel> channel();

	SocketAddress address();

}