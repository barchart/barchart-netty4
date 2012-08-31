package com.barchart.netty.part.hand;

import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;
import io.netty.channel.socket.DatagramPacket;

public class DatagramPacketReader extends ChannelHandlerAdapter implements
		ChannelInboundMessageHandler<Object> {

	@Override
	public MessageBuf<Object> newInboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		return Unpooled.messageBuffer();
	}

	@Override
	public final void inboundBufferUpdated(final ChannelHandlerContext ctx)
			throws Exception {

		if (ctx.hasInboundMessageBuffer()) {
			processMessageBuffer(ctx);
		}

		ctx.fireInboundBufferUpdated();

	}

	private void processMessageBuffer(final ChannelHandlerContext ctx) {

		final MessageBuf<Object> source = ctx.inboundMessageBuffer();

		final MessageBuf<Object> target = ctx.nextInboundMessageBuffer();

		while (true) {

			final Object message = source.poll();

			if (message == null) {
				break;
			}

			if (message instanceof DatagramPacket) {
				final DatagramPacket packet = (DatagramPacket) message;
				target.add(packet.data());
			} else {
				target.add(message);
			}

		}

	}

}
