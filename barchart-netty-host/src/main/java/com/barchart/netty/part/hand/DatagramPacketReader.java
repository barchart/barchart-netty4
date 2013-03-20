package com.barchart.netty.part.hand;

import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;
import io.netty.channel.ChannelStateHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

/** from DatagramPacket into ByteBuf, initial stage after read */
public class DatagramPacketReader extends ChannelStateHandlerAdapter implements
		ChannelInboundMessageHandler<Object> {

	@Override
	public MessageBuf<Object> newInboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		return Unpooled.messageBuffer();
	}

	@Override
	public final void inboundBufferUpdated(final ChannelHandlerContext ctx)
			throws Exception {

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

		ctx.fireInboundBufferUpdated();

	}

	@Override
	public void freeInboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		// TODO Auto-generated method stub
	}

}
