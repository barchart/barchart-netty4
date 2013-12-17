package com.barchart.netty.client.base;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observer;

import com.barchart.netty.client.Connectable;

public class ReconnectPolicy implements Observer<Connectable.StateChange<?>> {

	private static final Logger log = LoggerFactory
			.getLogger(ReconnectPolicy.class);

	private final ScheduledExecutorService executor;

	private final int attempts;
	private final long delay;
	private final TimeUnit unit;

	private int attemptsMade = 0;

	public ReconnectPolicy(final ScheduledExecutorService executor_,
			final long delay_, final TimeUnit unit_) {

		this(executor_, delay_, unit_, -1);

	}

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
