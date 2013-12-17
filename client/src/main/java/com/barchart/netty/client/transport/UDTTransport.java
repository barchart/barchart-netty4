package com.barchart.netty.client.transport;

import io.netty.channel.udt.nio.NioUdtByteConnectorChannel;

import java.net.URI;

public class UDTTransport extends SimpleTransport {

	protected UDTTransport(final URI uri) {
		super(uri, NioUdtByteConnectorChannel.class);
	}

}
