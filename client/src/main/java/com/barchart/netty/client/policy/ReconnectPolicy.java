/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.client.policy;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observer;

import com.barchart.netty.client.Connectable;

/**
 * An automatic reconnect policy for a connection. When a disconnect event is
 * received, it will attempt to re-establish the connection after the specified
 * delay.
 * 
 * To use, add as a connection state listener on a Connectable.
 * 
 * <pre>client.stateChanges().subscribe(new ReconnectPolicy(executor, 5, TimeUnit.SECONDS));</pre>
 */
public class ReconnectPolicy implements Observer<Connectable.StateChange<?>> {

	private static final Logger log = LoggerFactory
			.getLogger(ReconnectPolicy.class);

	private final ScheduledExecutorService executor;

	private final int attempts;
	private final long delay;
	private final TimeUnit unit;

	private int attemptsMade = 0;

	/**
	 * Reconnect a Connectable after the specified delay when a disconnect event
	 * is received.
	 * 
	 * @param executor_ The scheduled executor for the reconnect delay
	 */
	public ReconnectPolicy(final ScheduledExecutorService executor_,
			final long delay_, final TimeUnit unit_) {

		this(executor_, delay_, unit_, -1);

	}

	/**
	 * Reconnect a Connectable after the specified delay when a disconnect event
	 * is received, but only a limited number of attempts.
	 * 
	 * @param executor_ The scheduled executor for the reconnect delay
	 * @param attempts_ The maximum number of retries
	 */
	public ReconnectPolicy(final ScheduledExecutorService executor_,
			final long delay_, final TimeUnit unit_, final int attempts_) {

		executor = executor_;
		delay = delay_;
		unit = unit_;
		attempts = attempts_;

	}

	@Override
	public void onNext(final Connectable.StateChange<?> change) {

		switch (change.state()) {

			case DISCONNECTED:
			case CONNECT_FAIL:

				if (attempts == -1 || attemptsMade <= attempts) {

					executor.schedule(new Runnable() {
						@Override
						public void run() {
							attemptsMade++;
							log.info(change.state().name()
									+ ": reconnecting (attempt #"
									+ attemptsMade + ")");
							change.connectable().connect();
						}
					}, delay, unit);

				} else {

					log.warn(change.state().name()
							+ ": reached max reconnect attempts of " + attempts);

				}

				break;

			case CONNECTED:
				attemptsMade = 0;
				break;

			default:

		}

	}

	@Override
	public void onCompleted() {
	}

	@Override
	public void onError(final Throwable e) {
	}

}
