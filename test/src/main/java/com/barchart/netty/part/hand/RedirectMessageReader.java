package com.barchart.netty.part.hand;

import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelStateHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** redirect incoming messages into a different pipeline */
public class RedirectMessageReader extends ChannelStateHandlerAdapter implements
		ChannelInboundMessageHandler<Object> {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final ChannelPipeline targetPipeline;

	public RedirectMessageReader(final ChannelPipeline targetPipeline) {

		this.targetPipeline = targetPipeline;

	}

	@Override
	public MessageBuf<Object> newInboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		return Unpooled.messageBuffer(10);
	}

	@Override
	public final void inboundBufferUpdated(final ChannelHandlerContext ctx)
			throws Exception {

		final MessageBuf<Object> source = ctx.inboundMessageBuffer();

		final MessageBuf<Object> target = targetPipeline.inboundMessageBuffer();

		while (true) {

			final Object message = source.poll();

			if (message == null) {
				break;
			}

			target.add(message);

		}

		targetPipeline.fireInboundBufferUpdated();

	}

}
