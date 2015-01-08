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

public class TimeoutPromiseGroup extends DefaultPromise<Void> {

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

			done = true;
			setSuccess(null);

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
		synchronized (this) {
			if (!done && complete.incrementAndGet() == expected) {
				done = true;
				if (timeoutFuture != null) {
					timeoutFuture.cancel(true);
				}
				setSuccess(null);
			}
		}
	}

	private void fail(final Throwable t) {
		synchronized (this) {
			if (!done) {
				done = true;
				if (timeoutFuture != null) {
					timeoutFuture.cancel(true);
				}
				setFailure(t);
			}
		}
	}

	class FutureListener implements GenericFutureListener<Future<Object>> {

		@Override
		public void operationComplete(final Future<Object> future) throws Exception {
			try {
				future.get();
				success();
			} catch (final InterruptedException ie) {
				fail(ie);
			} catch (final ExecutionException ee) {
				fail(ee.getCause());
			}
		}

	}

	class TimeoutHandler implements Runnable {

		@Override
		public void run() {
			if (complete.get() != expected) {
				fail(new TimeoutException("Timeout expired before promises completed"));
			}
		}

	}

}
