package com.barchart.netty.part.hand;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundMessageHandler;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.part.dot.DotAny;

public class DatagramPacketWriter extends ChannelHandlerAdapter implements
		ChannelOutboundMessageHandler<Object> {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private InetSocketAddress localAddress;
	private InetSocketAddress remoteAddress;

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {

		localAddress = ctx.channel().attr(DotAny.LOCAL_ADDRESS).get();
		remoteAddress = ctx.channel().attr(DotAny.REMOTE_ADDRESS).get();

		super.channelActive(ctx);

	}

	@Override
	public MessageBuf<Object> newOutboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		return Unpooled.messageBuffer();
	}

	@Override
	public void flush(final ChannelHandlerContext ctx,
			final ChannelFuture future) throws Exception {

		if (ctx.hasOutboundMessageBuffer()) {
			processMessageBuffer(ctx);
		}

		ctx.flush(future);

	}

	private void processMessageBuffer(final ChannelHandlerContext ctx) {

		final MessageBuf<Object> source = ctx.outboundMessageBuffer();

		final MessageBuf<Object> target = ctx.nextOutboundMessageBuffer();

		while (true) {

			final Object entry = source.poll();

			if (entry == null) {
				break;
			}

			if (entry instanceof ByteBuf) {

				final ByteBuf buffer = (ByteBuf) entry;

				final DatagramPacket packet = new DatagramPacket(buffer,
						remoteAddress);

				target.add(packet);

			} else {

				target.add(entry);

			}

		}

	}

}
