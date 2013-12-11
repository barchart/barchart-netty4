package com.barchart.netty.client.transport;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

public class SimpleTransport implements TransportProtocol {

	private final Class<? extends Channel> channel;

	protected SimpleTransport(final Class<? extends Channel> channel_) {
		channel = channel_;
	}

	@Override
	public Class<? extends Channel> channel() {
		return channel;
	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {
		pipeline.addLast(new SimpleConnectedNotifier());
	}

	private class SimpleConnectedNotifier extends PassthroughStateHandler {

		@Override
		public void channelActive(final ChannelHandlerContext ctx)
				throws Exception {

			ctx.fireUserEventTriggered(Event.CONNECTED);
			ctx.pipeline().remove(this);

		}

	}

}