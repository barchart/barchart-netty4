package com.barchart.netty.part.hand;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SequenceWriter extends ChannelHandlerAdapter {

	private final AtomicLong counter = new AtomicLong(0);

	private ChannelHandlerContext ctx;

	private ScheduledFuture<?> writeFuture;

	private final Runnable writeTask = new Runnable() {
		@Override
		public void run() {
			writeSequence();
		}
	};

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {

		this.ctx = ctx;

		writeActive();

	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx)
			throws Exception {

		writeInactive();

		this.ctx = null;

	}

	protected void writeActive() {

		writeFuture = ctx.channel().eventLoop()
				.scheduleAtFixedRate(writeTask, 0, 1, TimeUnit.SECONDS);

	}

	protected void writeInactive() {

		final ScheduledFuture<?> future = this.writeFuture;

		if (future == null) {
			return;
		}

		future.cancel(true);

	}

	protected void writeSequence() {

		final ChannelHandlerContext ctx = this.ctx;

		if (ctx == null || ctx.channel() == null || !ctx.channel().isActive()) {
			return;
		}

		ctx.write(makeSequence());

		ctx.flush();

	}

	protected String makeSequence() {

		return "sequence=" + counter.getAndIncrement();

	}

}
