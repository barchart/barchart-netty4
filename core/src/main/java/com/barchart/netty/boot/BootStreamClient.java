package com.barchart.netty.boot;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.api.NettyPipe;
import com.barchart.netty.util.point.NetPoint;

/**
 * parent for connection oriented client end points
 * 
 * such as TCP
 */
@Component(name = BootStreamClient.TYPE, immediate = true)
public class BootStreamClient extends BootAny {

	public static final String TYPE = "barchart.netty.boot.stream.client";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public ChannelFuture startup(final NetPoint netPoint) throws Exception {

		return new Bootstrap()

		.channel(NioSocketChannel.class)

		.group(group())

		.localAddress(netPoint.getLocalAddress())

		.remoteAddress(netPoint.getRemoteAddress())

		.option(ChannelOption.SO_REUSEADDR, true)

		.option(ChannelOption.SO_SNDBUF, //
				netPoint.getSendBufferSize())

		.option(ChannelOption.SO_RCVBUF, //
				netPoint.getReceiveBufferSize())

		/** connector */
		.handler(pipeApply(netPoint, NettyPipe.Mode.DEFAULT))

		.connect();

	}

}
