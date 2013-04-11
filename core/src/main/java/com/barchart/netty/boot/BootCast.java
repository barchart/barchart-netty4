package com.barchart.netty.boot;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioDatagramChannel;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.api.NettyPipe;
import com.barchart.netty.util.point.NetPoint;

/**
 * parent for datagram based connection-less end points;
 * 
 * such as:
 * 
 * anycast, broadcast, unicast, multicast, etc;
 */
@Component(name = BootCast.TYPE, immediate = true)
public class BootCast extends BootAny {

	public static final String TYPE = "barchart.netty.boot.cast";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public ChannelFuture startup(final NetPoint netPoint) throws Exception {

		return new Bootstrap()

		.channel(NioDatagramChannel.class)

		.group(group())

		.localAddress(netPoint.getLocalAddress())

		.remoteAddress(netPoint.getRemoteAddress())

		.option(ChannelOption.SO_SNDBUF, //
				netPoint.getSendBufferSize())

		.option(ChannelOption.SO_RCVBUF, //
				netPoint.getReceiveBufferSize())

		.option(ChannelOption.SO_REUSEADDR, true)

		.option(ChannelOption.IP_MULTICAST_TTL,//
				netPoint.getPacketTTL())

		.handler(pipeApply(netPoint, NettyPipe.Mode.DEFAULT))

		.bind()

		.sync();

	}

	@Override
	public ChannelFuture shutdown(final NetPoint netPoint, final Channel channel)
			throws Exception {

		return channel.close().sync();

	}

}
