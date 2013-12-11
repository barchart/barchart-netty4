package com.barchart.netty.client.base;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import rx.Observer;

import com.barchart.netty.client.Connectable;
import com.barchart.netty.client.Ping;
import com.barchart.netty.client.Pong;
import com.barchart.netty.client.TimeSensitive;
import com.barchart.netty.client.transport.TransportProtocol;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * A Connectable client that provides heartbeat functionality both for keeping
 * the connection alive (preventing read timeouts) and measuring latency and
 * clock skew between peers. Implementations must provide a pipeline codec in
 * their initPipeline() method for sending and receiving Timestamp messages.
 * 
 * Note that in order to properly measure latency and clock skew, the remote
 * host must response to any Timestamp messages with a Timestamp response of
 * their own as quickly as possible.
 */
public abstract class KeepaliveConnectableBase<T extends KeepaliveConnectableBase<T>>
		extends ConnectableBase<T> implements TimeSensitive, Connectable<T> {

	/* Heartbeat interval */
	private long interval = 0;

	/* Peer state */
	private final long latency = 0;
	private final Histogram latencySampler = new MetricsRegistry()
			.newHistogram(KeepaliveConnectableBase.this.getClass(),
					"connectable-latency", true);
	private final long clockSkew = 0;

	/* Heartbeat sender */
	private final ConnectionHeartbeat monitor;

	/**
	 * Create a new latency-aware Connectable.
	 */
	protected KeepaliveConnectableBase(final EventLoopGroup eventLoop_,
			final InetSocketAddress address_, final TransportProtocol transport_) {

		super(eventLoop_, address_, transport_);
		monitor = new ConnectionHeartbeat();
		stateChanges().subscribe(monitor);

	}

	/**
	 * Get the current heartbeat interval in seconds. 0 means disabled.
	 */
	protected long heartbeatInterval() {
		return interval;
	}

	/**
	 * Set the current heartbeat interval in seconds. Set to 0 to disable.
	 */
	protected void heartbeatInterval(final long interval_) {
		interval = interval_;
		monitor.restartHeartbeat();
	}

	@Override
	public double averageLatency() {
		return latencySampler.mean();
	}

	@Override
	public long latency() {
		return latency;
	}

	@Override
	public long clockSkew() {
		return clockSkew;
	}

	private class ConnectionHeartbeat implements Observer<Connectable.State> {

		private ScheduledFuture<?> heartbeat = null;

		private final Runnable heartbeatSender = new Runnable() {

			@Override
			public void run() {
				send(new Ping() {

					@Override
					public long timestamp() {
						return System.currentTimeMillis();
					}

				});
			}

		};

		private final Observer<Pong> heartbeatHandler = new Observer<Pong>() {

			@Override
			public void onNext(final Pong timestamp) {
				// Compare the peer-received timestamp to current
				final long latency =
						(System.currentTimeMillis() - timestamp.pinged()) / 2;
				latencySampler.update(latency);
			}

			@Override
			public void onCompleted() {
				// Should never happen
			}

			@Override
			public void onError(final Throwable t) {
				// Should never happen
			}

		};

		public ConnectionHeartbeat() {
			receive(Pong.class).subscribe(heartbeatHandler);
		}

		@Override
		public void onNext(final Connectable.State state) {

			switch (state) {

				case CONNECTED:
					startHeartbeat();
					break;

				default:
					stopHeartbeat();

			}

		}

		@Override
		public void onCompleted() {
			stopHeartbeat();
		}

		@Override
		public void onError(final Throwable e) {
			stopHeartbeat();
		}

		public void restartHeartbeat() {

			if (heartbeat != null) {
				startHeartbeat();
			}

		}

		public void startHeartbeat() {

			synchronized (this) {

				if (heartbeat != null) {
					heartbeat.cancel(false);
				}

				if (heartbeatInterval() > 0) {
					heartbeat =
							channel.eventLoop().scheduleAtFixedRate(
									heartbeatSender, heartbeatInterval(),
									heartbeatInterval(), TimeUnit.SECONDS);
				}

			}

		}

		public void stopHeartbeat() {

			synchronized (this) {
				if (heartbeat != null) {
					heartbeat.cancel(false);
					heartbeat = null;
				}
			}

		}

	}

}
