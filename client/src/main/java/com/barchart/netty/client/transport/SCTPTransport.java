package com.barchart.netty.client.transport;

import io.netty.channel.sctp.nio.NioSctpChannel;

import java.net.URI;

public class SCTPTransport extends SimpleTransport {

	protected SCTPTransport(final URI uri) {
		super(uri, NioSctpChannel.class);
	}

}
