package com.barchart.netty.server.util;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutPromiseGroup extends DefaultPromise<Void> {

	private final static Logger log = LoggerFactory.getLogger(TimeoutPromiseGroup.class);

	private final int expected;
	private final AtomicInteger complete = new AtomicInteger(0);

	private volatile boolean done;

	private ScheduledFuture<?> timeoutFuture;

	public TimeoutPromiseGroup(final EventExecutor executor, final Future<?>... futures) {
		this(executor, 0, TimeUnit.SECONDS, Arrays.asList(futures));
	}

	public TimeoutPromiseGroup(final EventExecutor executor, final List<Future<?>> futures) {
		this(executor, 0, TimeUnit.SECONDS, futures);
	}

	public TimeoutPromiseGroup(final EventExecutor executor, final long timeout, final TimeUnit units,
			final Future<?>... futures) {
		this(executor, timeout, units, Arrays.asList(futures));
	}

	public TimeoutPromiseGroup(final EventExecutor executor, final long timeout, final TimeUnit units,
			final List<Future<?>> futures) {

		super(executor);

		expected = futures.size();

		if (expected == 0) {

			log.debug("no futures passed, succeeding");
			success();

		} else {

			final FutureListener pl = new FutureListener();

			for (final Future<?> future : futures) {
				future.addListener(pl);
			}

			if (timeout > 0) {
				timeoutFuture = executor.schedule(new TimeoutHandler(), timeout, units);
			}

		}

	}

	private void success() {
		log.debug("success");
		synchronized (this) {
			log.debug("success sync");
			if (!done && complete.incrementAndGet() == expected) {
				done = true;
				if (timeoutFuture != null) {
					timeoutFuture.cancel(true);
				}
				setSuccess(null);
			}
		}
		log.debug("success out");
	}

	private void fail(final Throwable t) {
		log.debug("fail");
		synchronized (this) {
			log.debug("fail sync");
			if (!done) {
				done = true;
				if (timeoutFuture != null) {
					timeoutFuture.cancel(true);
				}
				setFailure(t);
			}
		}
		log.debug("fail out");
	}

	class FutureListener implements GenericFutureListener<Future<Object>> {

		@Override
		public void operationComplete(final Future<Object> future) throws Exception {
			log.debug("FutureListener");
			try {
				future.get();
				log.debug("FutureListener success");
				success();
			} catch (final InterruptedException ie) {
				log.debug("FutureListener fail", ie);
				fail(ie);
			} catch (final ExecutionException ee) {
				log.debug("FutureListener fail", ee);
				fail(ee.getCause());
			}
		}

	}

	class TimeoutHandler implements Runnable {

		@Override
		public void run() {
			log.debug("TimeoutHandler, expected=" + expected);
			if (complete.get() != expected) {
				log.debug("TimeoutHandler expired");
				fail(new TimeoutException("Timeout expired before promises completed"));
			}
		}

	}

}
