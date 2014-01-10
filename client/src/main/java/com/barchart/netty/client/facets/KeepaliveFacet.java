package com.barchart.netty.client.facets;

import io.netty.channel.ChannelPipeline;

import java.util.concurrent.TimeUnit;

import com.barchart.netty.client.pipeline.PingHandler;

/**
 * A Connectable proxy facet that implements the LatencyAware interface. To
 * provide this functionality, this facet provides a connection heartbeat both
 * for keeping the connection alive (preventing read timeouts) and measuring
 * latency and clock skew between peers.
 * 
 * Proper functionality of this facet requires that:
 * 
 * 1) The client and host both understand Ping and Pong messages
 * 
 * 2) The host returns a Pong message immediately on receipt of a Ping
 * 
 * @see com.barchart.netty.common.messages.Ping
 * @see com.barchart.netty.common.messages.Pong
 */
public class KeepaliveFacet implements ConnectableFacet<LatencyAware>,
		LatencyAware {

	private PingHandler pingHandler = null;

	/* Heartbeat interval */
	private long interval;
	private TimeUnit unit;

	public KeepaliveFacet(final long interval_, final TimeUnit unit_) {
		interval(interval_, unit_);
	}

	@Override
	public Class<LatencyAware> type() {
		return LatencyAware.class;
	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {

		if (interval > 0) {
			pingHandler = new PingHandler(interval, unit);
			pipeline.addLast(pingHandler);
		}

	}

	public void interval(final long interval_, final TimeUnit unit_) {

		interval = interval_;
		unit = unit_;

		if (pingHandler != null) {
			pingHandler.interval(interval_, unit_);
		}

	}

	@Override
	public double averageLatency() {

		if (pingHandler != null) {
			return pingHandler.averageLatency();
		}

		return -1;

	}

	@Override
	public long latency() {

		if (pingHandler != null) {
			return pingHandler.latency();
		}

		return -1;

	}

	@Override
	public long clockSkew() {

		if (pingHandler != null) {
			return pingHandler.clockSkew();
		}

		return 0;

	}

	@Override
	public long peerTime() {

		if (pingHandler != null) {
			return pingHandler.peerTime();
		}

		return System.currentTimeMillis();

	}

}
