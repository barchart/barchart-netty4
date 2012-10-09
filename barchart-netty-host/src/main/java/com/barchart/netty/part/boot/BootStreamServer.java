package com.barchart.netty.part.boot;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.host.api.NettyPipe;
import com.barchart.netty.util.point.NetPoint;

@Component(name = BootStreamServer.TYPE)
public class BootStreamServer extends BootAny {

	public static final String TYPE = "barchart.netty.boot.stream.server";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public ChannelFuture boot(final NetPoint netPoint) throws Exception {

		return new ServerBootstrap()
				.group(group())
				.channel(NioServerSocketChannel.class)
				.localAddress(netPoint.getLocalAddress())
				.option(ChannelOption.SO_BACKLOG, 100)
				.option(ChannelOption.SO_SNDBUF, netPoint.getSendBufferSize())
				.option(ChannelOption.SO_RCVBUF,
						netPoint.getReceiveBufferSize())
				.childOption(ChannelOption.SO_SNDBUF,
						netPoint.getSendBufferSize())
				.childOption(ChannelOption.SO_RCVBUF,
						netPoint.getReceiveBufferSize())

				/** acceptor a.k.a server a.k.a parent a.k.a default */
				.handler(pipeApply(netPoint, NettyPipe.Mode.DEFAULT))

				/** connector a.k.a client a.k.a child a.k.a managed */
				.childHandler(pipeApply(netPoint, NettyPipe.Mode.DERIVED))
				.bind();

	}

}
