package com.barchart.netty.client.transport;

import io.netty.buffer.MessageBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandlerUtil;
import io.netty.channel.ChannelStateHandlerAdapter;

public class PassthroughStateHandler extends ChannelStateHandlerAdapter {

	@Override
	public void inboundBufferUpdated(final ChannelHandlerContext ctx)
			throws Exception {

		final MessageBuf<Object> in = ctx.inboundMessageBuffer();

		for (;;) {
			final Object msg = in.poll();
			if (msg == null) {
				break;
			}
			ChannelHandlerUtil.addToNextInboundBuffer(ctx, in.poll());
		}

		ctx.fireInboundBufferUpdated();

	}

}