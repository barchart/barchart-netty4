package com.barchart.netty.part.boot;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.sctp.SctpChannelOption;
import io.netty.channel.sctp.nio.NioSctpServerChannel;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.host.api.NettyPipe;
import com.barchart.netty.util.point.NetPoint;

/**
 * parent for connection oriented server end points
 * 
 * such as SCTP
 */
@Component(name = BootStormServer.TYPE, immediate = true)
public class BootStormServer extends BootAny {

	public static final String TYPE = "barchart.netty.boot.storm.server";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public ChannelFuture startup(final NetPoint netPoint) throws Exception {

		return new ServerBootstrap()

		.channel(NioSctpServerChannel.class)

		.group(group())

		.localAddress(netPoint.getLocalAddress())

		.option(ChannelOption.SO_BACKLOG, 100)

		.childOption(SctpChannelOption.SCTP_NODELAY, true)

		/** https://github.com/netty/netty/issues/610 */

		.childOption(ChannelOption.SO_SNDBUF, netPoint.getSendBufferSize())

		.childOption(ChannelOption.SO_RCVBUF, netPoint.getReceiveBufferSize())

		/** acceptor a.k.a server a.k.a parent a.k.a default */
		.handler(pipeApply(netPoint, NettyPipe.Mode.DEFAULT))

		/** connector a.k.a client a.k.a child a.k.a managed */
		.childHandler(pipeApply(netPoint, NettyPipe.Mode.DERIVED))

		.bind();

	}

}
