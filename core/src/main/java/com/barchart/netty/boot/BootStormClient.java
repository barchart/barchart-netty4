/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.boot;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.sctp.SctpChannelOption;
import io.netty.channel.sctp.nio.NioSctpChannel;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.api.NettyPipe;
import com.barchart.netty.util.point.NetPoint;

/**
 * parent for connection oriented client end points
 * 
 * such as SCTP
 */
@Component(name = BootStormClient.TYPE, immediate = true)
public class BootStormClient extends BootAny {

	public static final String TYPE = "barchart.netty.boot.storm.client";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public ChannelFuture startup(final NetPoint netPoint) throws Exception {

		return new Bootstrap().localAddress(netPoint.getLocalAddress())

		.channel(NioSctpChannel.class)

		.group(group())

		.remoteAddress(netPoint.getRemoteAddress())

		.option(SctpChannelOption.SCTP_NODELAY, true)

		/** https://github.com/netty/netty/issues/610 */

		.option(ChannelOption.SO_SNDBUF, //
				netPoint.getSendBufferSize())

		.option(ChannelOption.SO_RCVBUF, //
				netPoint.getReceiveBufferSize())

		/** connector */
		.handler(pipeApply(netPoint, NettyPipe.Mode.DEFAULT))

		.connect();

	}

	@Override
	public ChannelFuture shutdown(final NetPoint netPoint, final Channel channel)
			throws Exception {

		return channel.close();

	}

}
