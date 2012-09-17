package com.barchart.netty.part.hand;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;
import io.netty.channel.ChannelOutboundMessageHandler;
import io.netty.channel.socket.SctpData;

/** ByteBuf-SctpData wrapper */
public class SctpMessageCodec extends ChannelHandlerAdapter implements
		ChannelInboundMessageHandler<Object>,
		ChannelOutboundMessageHandler<Object> {

	@Override
	public MessageBuf<Object> newOutboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		return Unpooled.messageBuffer();
	}

	@Override
	public MessageBuf<Object> newInboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		return Unpooled.messageBuffer();
	}

	@Override
	public void inboundBufferUpdated(final ChannelHandlerContext ctx)
			throws Exception {

		final MessageBuf<Object> source = ctx.inboundMessageBuffer();

		final MessageBuf<Object> target = ctx.nextInboundMessageBuffer();

		while (true) {

			final Object entry = source.poll();

			if (entry == null) {
				break;
			}

			if (entry instanceof SctpData) {

				final SctpData data = (SctpData) entry;

				final ByteBuf buffer = data.getPayloadBuffer();

				target.add(buffer);

			}

		}

		ctx.fireInboundBufferUpdated();

	}

	@Override
	public void flush(final ChannelHandlerContext ctx,
			final ChannelFuture future) throws Exception {

		final MessageBuf<Object> source = ctx.outboundMessageBuffer();

		final MessageBuf<Object> target = ctx.nextOutboundMessageBuffer();

		while (true) {

			final Object entry = source.poll();

			if (entry == null) {
				break;
			}

			if (entry instanceof ByteBuf) {

				final SctpData data = new SctpData(0, 0, (ByteBuf) entry);

				target.add(data);
			}

		}

		ctx.flush(future);

	}

}
