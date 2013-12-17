package com.barchart.netty.client.base;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;

import java.util.concurrent.TimeUnit;

import com.barchart.netty.client.Connectable;
import com.barchart.netty.client.facets.LatencyAware;
import com.barchart.netty.client.pipeline.PingHandler;
import com.barchart.netty.client.transport.TransportProtocol;

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
		extends SecureConnectableBase<T> implements LatencyAware,
		Connectable<T> {

	protected KeepaliveConnectableBase(final EventLoopGroup eventLoop_,
			final TransportProtocol transport_) {
		super(eventLoop_, transport_);
	}

	protected abstract static class Builder<B extends Builder<B, C>, C extends KeepaliveConnectableBase<C>>
			extends SecureConnectableBase.Builder<B, C> {

		protected long interval;
		protected TimeUnit unit;

		@SuppressWarnings("unchecked")
		public B ping(final long interval_, final TimeUnit unit_) {
			interval = interval_;
			unit = unit_;
			return (B) this;
		}

		@Override
		protected C configure(final C client) {
			super.configure(client);
			client.pingHandler = new PingHandler(interval, unit);
			return client;
		}
	}

	/* Heartbeat interval */
	private PingHandler pingHandler = null;

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {

		super.initPipeline(pipeline);

		if (pingHandler != null) {
			pipeline.addLast(pingHandler);
		}

	}

	@Override
	public double averageLatency() {
		return pingHandler.averageLatency();
	}

	@Override
	public long latency() {
		return pingHandler.latency();
	}

	@Override
	public long clockSkew() {
		return pingHandler.clockSkew();
	}

	@Override
	public long peerTime() {
		return pingHandler.peerTime();
	}

}
