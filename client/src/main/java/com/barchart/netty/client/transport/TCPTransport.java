package com.barchart.netty.client.transport;

import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.URI;

public class TCPTransport extends SimpleTransport {

	protected TCPTransport(final URI uri) {
		super(uri, NioSocketChannel.class);
	}

}
