package com.barchart.netty.test.sequence;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandSequenceWriter extends ChannelHandlerAdapter {

	protected final Logger log = LoggerFactory.getLogger(getClass());

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
				.scheduleAtFixedRate(writeTask, 1, 1, TimeUnit.SECONDS);

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

		final String message = makeSequence();

		log.info("wirter message : {}", message);

		ctx.write(message);

	}

	static final String PREFIX = "sequence=";

	protected String makeSequence() {

		return PREFIX + counter.getAndIncrement();

	}

}
