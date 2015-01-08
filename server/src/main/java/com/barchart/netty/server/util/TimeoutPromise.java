package com.barchart.netty.server.util;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutPromise extends DefaultPromise<Void> {

	private final static Logger log = LoggerFactory.getLogger(TimeoutPromise.class);

	private volatile boolean done;

	private ScheduledFuture<?> timeoutFuture;

	public TimeoutPromise(final EventExecutor executor, final long timeout, final TimeUnit units,
			final Future<?> future) {

		super(executor);

		future.addListener(new FutureListener());

		if (timeout > 0) {
			timeoutFuture = executor.schedule(new TimeoutHandler(), timeout, units);
		}

	}

	private void success() {
		log.debug("success");
		synchronized (this) {
			log.debug("success sync");
			if (!done) {
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
			log.debug("TimeoutHandler expired");
			fail(new TimeoutException("Timeout expired before promises completed"));
		}

	}

}
