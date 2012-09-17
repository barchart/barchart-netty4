package com.barchart.netty.test.arb;

import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.util.point.NetPoint;

/**
 * duplicate message arbiter handler
 * 
 * FIXME need proto-buf awareness
 */
public class HandArb extends ChannelHandlerAdapter implements
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

		final NettyDot dot = ctx.channel().attr(NettyDot.ATTR_NETTY_DOT).get();

		final NetPoint point = dot.netPoint();

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

			collectMessage(message);

		}

		if (isCollectReady()) {

			target.addAll(collectBuffer());

			ctx.fireInboundBufferUpdated();

		} else {

			startCollectTimer();

		}

	}

	/** collect timer will force disruptor buffer flush when timeout expires */
	private void startCollectTimer() {

	}

	/** drain collected messages from disruptor buffer */
	private List<Object> collectBuffer() {
		return new ArrayList<Object>();
	}

	/** disruptor has no losses or is timeout expired */
	private boolean isCollectReady() {
		return false;
	}

	/** store message in disruptor, handle duplicates, etc */
	private void collectMessage(final Object message) {

	}

}
