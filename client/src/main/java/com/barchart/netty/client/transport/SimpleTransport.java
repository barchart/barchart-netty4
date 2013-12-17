package com.barchart.netty.client.transport;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelStateHandlerAdapter;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;

public class SimpleTransport implements TransportProtocol {

	private final URI uri;
	private final InetSocketAddress address;
	private final Class<? extends Channel> channel;

	protected SimpleTransport(final URI uri_,
			final Class<? extends Channel> channel_) {

		uri = uri_;

		if (uri.getPort() == -1) {
			throw new IllegalArgumentException(
					"Port must be specified, no default port for '"
							+ uri.getScheme() + "'");
		}

		address = new InetSocketAddress(uri.getHost(), uri.getPort());
		channel = channel_;

	}

	@Override
	public Class<? extends Channel> channel() {
		return channel;
	}

	@Override
	public SocketAddress address() {
		return address;
	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {
		pipeline.addLast(new SimpleConnectedNotifier());
	}

	private class SimpleConnectedNotifier extends ChannelStateHandlerAdapter {

		@Override
		public void channelActive(final ChannelHandlerContext ctx)
				throws Exception {

			ctx.fireUserEventTriggered(Event.CONNECTED);
			ctx.pipeline().remove(this);

			super.channelActive(ctx);

		}

		@Override
		public void inboundBufferUpdated(final ChannelHandlerContext ctx)
				throws Exception {
			ctx.fireInboundBufferUpdated();
		}

	}

}