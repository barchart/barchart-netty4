package com.barchart.netty.server.stream;

import io.netty.channel.ChannelFuture;
import io.netty.channel.udt.nio.NioUdtAcceptorChannel;

import java.net.SocketAddress;

import com.barchart.netty.server.base.AbstractStatefulServer;

public class UDTServer extends AbstractStatefulServer<UDTServer> {

	public UDTServer() {
		channel(NioUdtAcceptorChannel.class);
	}

	@Override
	public ChannelFuture listen(final SocketAddress address) {

		if (pipelineInit == null) {
			throw new IllegalStateException(
					"No pipeline initializer has been provided, server would do nothing");
		}

		return super.listen(address);

	}

}
