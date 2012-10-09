package com.barchart.netty.part.boot;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioDatagramChannel;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.barchart.netty.host.api.NettyPipe;
import com.barchart.netty.util.point.NetPoint;

/**
 * parent for datagram based connection-less end points;
 * 
 * such as:
 * 
 * anycast, broadcast, unicast, multicast, etc;
 */
@Component(name = BootCast.TYPE, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class BootCast extends BootAny {

	public static final String TYPE = "barchart.netty.boot.cast";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public ChannelFuture boot(final NetPoint netPoint) throws Exception {

		return new Bootstrap()
				.localAddress(netPoint.getLocalAddress())
				.remoteAddress(netPoint.getRemoteAddress())
				.channel(NioDatagramChannel.class)
				.option(ChannelOption.SO_SNDBUF, //
						netPoint.getSendBufferSize())
				.option(ChannelOption.SO_RCVBUF, //
						netPoint.getReceiveBufferSize())
				.option(ChannelOption.SO_REUSEADDR, true)
				.option(ChannelOption.IP_MULTICAST_TTL, netPoint.getPacketTTL())
				.group(group())
				.handler(pipeApply(netPoint, NettyPipe.Mode.DEFAULT)) //
				.bind() //
				.sync();

	}

	@Override
	public ChannelFuture shutdown(final NetPoint netPoint, final Channel channel)
			throws Exception {
		return channel.close().sync();
	}

}
