package com.barchart.netty.client.transport;

import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.URI;

public class UDPTransport extends SimpleTransport {

	protected UDPTransport(final URI uri) {
		super(uri, NioDatagramChannel.class);
	}

}
