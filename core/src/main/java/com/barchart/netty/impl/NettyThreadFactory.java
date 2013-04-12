package com.barchart.netty.impl;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/** FIXME temporary; replace with pools */
class NettyThreadFactory implements ThreadFactory {

	private final static AtomicLong count = new AtomicLong(0);

	@Override
	public Thread newThread(final Runnable task) {

		final String name = "# netty-" + count.getAndIncrement();

		final Thread thread = new Thread(task, name);

		return thread;

	}

}
