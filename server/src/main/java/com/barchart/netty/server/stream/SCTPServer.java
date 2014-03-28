/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.stream;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.sctp.SctpChannelOption;
import io.netty.channel.sctp.nio.NioSctpServerChannel;

import java.net.SocketAddress;

import com.barchart.netty.server.base.AbstractStatefulServer;

public class SCTPServer extends AbstractStatefulServer<SCTPServer> {

	public SCTPServer() {
		channel(NioSctpServerChannel.class);
	}

	@Override
	public ServerBootstrap bootstrap() {
		return super.bootstrap().childOption(SctpChannelOption.SCTP_NODELAY,
				true);
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
