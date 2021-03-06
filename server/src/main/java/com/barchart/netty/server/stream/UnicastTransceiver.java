/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.stream;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.barchart.netty.server.base.AbstractServer;

public class UnicastTransceiver extends
		AbstractServer<UnicastTransceiver, Bootstrap> {

	protected InetSocketAddress remote;

	public UnicastTransceiver remote(final InetSocketAddress address) {
		remote = address;
		return this;
	}

	@Override
	protected Bootstrap bootstrap() {

		final Bootstrap bootstrap = new Bootstrap() //
				.group(defaultGroup) //
				.channel(NioDatagramChannel.class) //
				.handler(new ServerChannelInitializer()) //
				.remoteAddress(remote) //
				.option(ChannelOption.SO_REUSEADDR, true) //
				.option(ChannelOption.IP_MULTICAST_TTL, 3) //
				.option(ChannelOption.SO_SNDBUF, 262144) //
				.option(ChannelOption.SO_RCVBUF, 262144);

		if (bootstrapInit != null) {
			bootstrapInit.initBootstrap(bootstrap);
		}

		return bootstrap;

	}

	@Override
	public ChannelFuture listen(final SocketAddress address) {

		if (pipelineInit == null) {
			throw new IllegalStateException(
					"No pipeline initializer has been provided, server would do nothing");
		}

		return super.listen(address);

	}

}
