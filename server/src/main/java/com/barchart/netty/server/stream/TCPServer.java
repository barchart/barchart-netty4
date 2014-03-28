/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.stream;

import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.SocketAddress;

import com.barchart.netty.server.base.AbstractStatefulServer;

public class TCPServer extends AbstractStatefulServer<TCPServer> {

	public TCPServer() {
		channel(NioServerSocketChannel.class);
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
