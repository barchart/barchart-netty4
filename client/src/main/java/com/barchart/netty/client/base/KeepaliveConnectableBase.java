/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.client.base;

import io.netty.channel.ChannelPipeline;

import java.util.concurrent.TimeUnit;

import com.barchart.netty.client.Connectable;
import com.barchart.netty.client.facets.KeepaliveFacet;
import com.barchart.netty.client.transport.TransportProtocol;
import com.barchart.netty.common.metadata.LatencyAware;

/**
 * A Connectable client that provides heartbeat functionality both for keeping
 * the connection alive (preventing read timeouts) and measuring latency and
 * clock skew between peers.
 * 
 * Proper functionality of this facet requires that:
 * 
 * 1) The client and host both understand Ping and Pong messages
 * 
 * 2) The host returns a Pong message immediately on receipt of a Ping
 * 
 * @see com.barchart.netty.client.facets.KeepaliveFacet
 */
public abstract class KeepaliveConnectableBase<T extends KeepaliveConnectableBase<T>>
		extends SecureConnectableBase<T> implements LatencyAware,
		Connectable<T> {

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
			client.facet = new KeepaliveFacet(interval, unit);
			return client;
		}
	}

	protected KeepaliveFacet facet = null;

	protected KeepaliveConnectableBase(final TransportProtocol transport_) {
		super(transport_);
	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {
		super.initPipeline(pipeline);
		facet.initPipeline(pipeline);
	}

	protected void interval(final long interval_, final TimeUnit unit_) {
		facet.interval(interval_, unit_);
	}

	@Override
	public double averageLatency() {
		return facet.averageLatency();
	}

	@Override
	public long latency() {
		return facet.latency();
	}

	@Override
	public long clockSkew() {
		return facet.clockSkew();
	}

	@Override
	public long peerTime() {
		return facet.peerTime();
	}

}
