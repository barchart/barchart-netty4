package com.barchart.netty.test.arb;

import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.util.arb.Arbiter;
import com.barchart.netty.util.arb.ArbiterCore;
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

	private final Arbiter<Object> arbiter = new ArbiterCore<Object>();

	@Override
	public final void inboundBufferUpdated(final ChannelHandlerContext ctx)
			throws Exception {

		final MessageBuf<Object> source = ctx.inboundMessageBuffer();

		while (true) {

			final Object message = source.poll();

			if (message == null) {
				break;
			}

			final long sequence = 0; // XXX

			arbiter.fill(sequence, message);

		}

		if (arbiter.isReady()) {

			timerOff();

			drain();

		} else {

			timerOn();

		}

	}

	private void timerOn() {

		if (future == null || future.isDone()) {
			future = ctx.channel().eventLoop()
					.schedule(task, 100, TimeUnit.MILLISECONDS);
		}

	}

	private void timerOff() {

		if (future == null || future.isDone()) {
			return;
		}

		future.cancel(true);
		future = null;

	}

	private ScheduledFuture<?> future;

	private final Runnable task = new Runnable() {
		@Override
		public void run() {
			drain();
		}
	};

	private void drain() {

		final MessageBuf<Object> target = ctx.nextInboundMessageBuffer();

		arbiter.drainTo(target);

		ctx.fireInboundBufferUpdated();

	}

}
