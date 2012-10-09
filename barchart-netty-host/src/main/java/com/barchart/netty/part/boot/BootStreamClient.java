package com.barchart.netty.part.boot;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.barchart.netty.host.api.NettyPipe;
import com.barchart.netty.util.point.NetPoint;

/**
 * parent for connection oriented client end points
 * 
 * such as TCP
 */
@Component(name = BootStreamClient.TYPE, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class BootStreamClient extends BootAny {

	public static final String TYPE = "barchart.netty.boot.stream.client";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public ChannelFuture boot(final NetPoint netPoint) throws Exception {

		return new Bootstrap()

				.localAddress(netPoint.getLocalAddress())
				.remoteAddress(netPoint.getRemoteAddress())
				.channel(NioSocketChannel.class)
				.option(ChannelOption.SO_REUSEADDR, true)
				.option(ChannelOption.SO_SNDBUF, netPoint.getSendBufferSize())
				.option(ChannelOption.SO_RCVBUF,
						netPoint.getReceiveBufferSize()).group(group())
				/** connector */
				.handler(pipeApply(netPoint, NettyPipe.Mode.DEFAULT)) //
				.connect();

	}

}
