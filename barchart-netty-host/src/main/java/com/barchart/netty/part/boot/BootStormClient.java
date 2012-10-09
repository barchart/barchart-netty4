package com.barchart.netty.part.boot;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSctpChannel;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.barchart.netty.host.api.NettyPipe;
import com.barchart.netty.util.point.NetPoint;

/**
 * parent for connection oriented client end points
 * 
 * such as SCTP
 */
@Component(name = BootStormClient.TYPE, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class BootStormClient extends BootAny {

	public static final String TYPE = "barchart.netty.boot.storm.client";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public ChannelFuture boot(final NetPoint netPoint) throws Exception {

		return new Bootstrap().localAddress(netPoint.getLocalAddress())
				.remoteAddress(netPoint.getRemoteAddress())
				.channel(NioSctpChannel.class)
				.option(ChannelOption.SCTP_NODELAY, true)
				/** https://github.com/netty/netty/issues/610 */
				.option(ChannelOption.SO_SNDBUF, //
						netPoint.getSendBufferSize())
				.option(ChannelOption.SO_RCVBUF, //
						netPoint.getReceiveBufferSize()).group(group())
				/** connector */
				.handler(pipeApply(netPoint, NettyPipe.Mode.DEFAULT)).connect();

	}

	@Override
	public ChannelFuture shutdown(final NetPoint netPoint, final Channel channel)
			throws Exception {
		return channel.close();
	}

}
