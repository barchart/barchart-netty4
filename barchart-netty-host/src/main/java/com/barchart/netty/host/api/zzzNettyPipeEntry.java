package com.barchart.netty.host.api;

import io.netty.channel.ChannelHandler;

import java.util.Map;

class zzzNettyPipeEntry implements Map.Entry<String, ChannelHandler> {

	private final String name;
	private final ChannelHandler handler;

	public zzzNettyPipeEntry(final String name, final ChannelHandler handler) {
		this.name = name;
		this.handler = handler;
	}

	@Override
	public String getKey() {
		return name;
	}

	@Override
	public ChannelHandler getValue() {
		return handler;
	}

	@Override
	public ChannelHandler setValue(final ChannelHandler value) {
		throw new UnsupportedOperationException();
	}

}
