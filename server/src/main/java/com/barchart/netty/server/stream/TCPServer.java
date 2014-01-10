package com.barchart.netty.server.stream;

import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.SocketAddress;

public class TCPServer extends StreamServer<TCPServer> {

	public TCPServer() {
		channel(NioServerSocketChannel.class);
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
