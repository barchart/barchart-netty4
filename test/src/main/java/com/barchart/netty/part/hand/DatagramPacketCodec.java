package com.barchart.netty.part.hand;

import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;
import io.netty.channel.ChannelOutboundMessageHandler;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;

public class DatagramPacketCodec extends ChannelDuplexHandler implements
		ChannelInboundMessageHandler<DatagramPacket>,
		ChannelOutboundMessageHandler<DatagramPacket> {

	@Override
	public MessageBuf<DatagramPacket> newOutboundBuffer(
			final ChannelHandlerContext ctx) throws Exception {
		return Unpooled.messageBuffer();
	}

	@Override
	public MessageBuf<DatagramPacket> newInboundBuffer(
			final ChannelHandlerContext ctx) throws Exception {
		return Unpooled.messageBuffer();
	}

	@Override
	public void inboundBufferUpdated(final ChannelHandlerContext ctx)
			throws Exception {

		final MessageBuf<DatagramPacket> buf = ctx.inboundMessageBuffer();

		final MessageBuf<Object> out = ctx.nextInboundMessageBuffer();
		for (;;) {
			final DatagramPacket o = buf.poll();
			if (o == null) {
				break;
			}
			out.add(o);
		}

		ctx.fireInboundBufferUpdated();

	}

	@Override
	public void flush(final ChannelHandlerContext ctx,
			final ChannelPromise promise) throws Exception {

		final MessageBuf<DatagramPacket> buf = ctx.outboundMessageBuffer();

		final MessageBuf<Object> out = ctx.nextOutboundMessageBuffer();

		for (;;) {
			final Object o = buf.poll();
			if (o == null) {
				break;
			}
			out.add(o);
		}

		ctx.flush(promise);

	}

}
