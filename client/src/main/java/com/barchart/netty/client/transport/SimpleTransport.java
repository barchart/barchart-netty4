/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.client.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;

public abstract class SimpleTransport implements TransportProtocol {

	private final URI uri;
	private final InetSocketAddress address;
	private final Class<? extends Channel> channel;

	protected SimpleTransport(final URI uri_,
			final Class<? extends Channel> channel_) {

		uri = uri_;

		if (uri.getPort() == -1) {
			throw new IllegalArgumentException(
					"Port must be specified, no default port for '"
							+ uri.getScheme() + "'");
		}

		address = new InetSocketAddress(uri.getHost(), uri.getPort());
		channel = channel_;

	}

	@Override
	public Class<? extends Channel> channel() {
		return channel;
	}

	@Override
	public SocketAddress address() {
		return address;
	}

	@Override
	public Bootstrap bootstrap() {
		return DEFAULT_BOOTSTRAP.clone().channel(channel())
				.remoteAddress(address());
	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {
	}

}