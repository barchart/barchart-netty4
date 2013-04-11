package com.barchart.netty.test.fail_over;

import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;
import io.netty.channel.ChannelStateHandlerAdapter;

import java.net.InetSocketAddress;

import com.barchart.netty.api.NettyDot;
import com.barchart.netty.util.point.NetPoint;

/**
 * channel fail-over / switch handler
 */
public class HandSwitch extends ChannelStateHandlerAdapter implements
		ChannelInboundMessageHandler<Object> {

	@Override
	public MessageBuf<Object> newInboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		return Unpooled.messageBuffer();
	}

	private ChannelHandlerContext ctx;

	private InetSocketAddress localAddress;
	private InetSocketAddress remoteAddress;

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {

		this.ctx = ctx;

		final NetPoint point =
				ctx.channel().attr(NettyDot.ATTR_NET_POINT).get();

		localAddress = point.getLocalAddress();
		remoteAddress = point.getRemoteAddress();

		super.channelActive(ctx);

	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx)
			throws Exception {

		super.channelInactive(ctx);

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

		}

	}

	@Override
	public void freeInboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		// TODO Auto-generated method stub
	}

}
