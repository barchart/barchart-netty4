package com.barchart.netty.test.arb;

import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.util.arb.Arbiter;
import com.barchart.netty.util.arb.ArbiterCore;
import com.barchart.netty.util.point.NetPoint;
import com.barchart.proto.buf.data.MarketPacket;

/**
 * duplicate message arbiter handler
 */
public class HandArb extends ChannelHandlerAdapter implements
		ChannelInboundMessageHandler<MarketPacket> {

	@Override
	public MessageBuf<MarketPacket> newInboundBuffer(
			final ChannelHandlerContext ctx) throws Exception {
		return Unpooled.messageBuffer();
	}

	private ChannelHandlerContext ctx;

	private int arbiterDepth;
	private int arbiterTimeout;
	private final TimeUnit arbiterUnit = TimeUnit.MILLISECONDS;

	private Arbiter<Object> arbiter;

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {

		this.ctx = ctx;

		final NettyDot dot = ctx.channel().attr(NettyDot.ATTR_NETTY_DOT).get();

		final NetPoint point = dot.netPoint();

		arbiterDepth = point.getInt("arbiter-depth", 10 * 1000);
		arbiterTimeout = point.getInt("arbiter-timeout", 200);

		arbiter = new ArbiterCore<Object>(arbiterDepth);

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

		final MessageBuf<MarketPacket> source = ctx.inboundMessageBuffer();

		while (true) {

			final MarketPacket message = source.poll();

			if (message == null) {
				break;
			}

			final long sequence = message.getSequence();

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
					.schedule(task, arbiterTimeout, arbiterUnit);
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
