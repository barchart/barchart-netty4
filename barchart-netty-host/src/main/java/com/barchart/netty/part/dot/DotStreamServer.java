package com.barchart.netty.part.dot;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.barchart.netty.host.api.NettyPipe;

/**
 * parent for connection oriented server end points
 * 
 * such as TCP
 */
@Component(name = DotStreamServer.NAME, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class DotStreamServer extends DotAny {

	public static final String NAME = "barchart.netty.dot.stream.server";

	@Override
	public String componentName() {
		return NAME;
	}

	private ServerBootstrap boot;

	protected ServerBootstrap boot() {
		return boot;
	}

	private NioServerSocketChannel channel;

	@Override
	public NioServerSocketChannel channel() {
		return channel;
	}

	protected ChannelFuture activateFuture;
	protected ChannelFuture deactivateFuture;

	@Override
	protected void activateBoot() throws Exception {

		boot = new ServerBootstrap();
		channel = new NioServerSocketChannel();

		boot().localAddress(localAddress());

		boot().option(ChannelOption.SO_BACKLOG, 100);

		boot().option(ChannelOption.SO_SNDBUF, netPoint().getSendBufferSize());
		boot().option(ChannelOption.SO_RCVBUF,
				netPoint().getReceiveBufferSize());

		boot().childOption(ChannelOption.SO_SNDBUF,
				netPoint().getSendBufferSize());
		boot().childOption(ChannelOption.SO_RCVBUF,
				netPoint().getReceiveBufferSize());

		boot().group(group());

		boot().channelFactory(new FixedChannelFactory(channel()));

		/** acceptor a.k.a server a.k.a parent a.k.a default */
		boot().handler(pipeApply(NettyPipe.Mode.DEFAULT));

		/** connector a.k.a client a.k.a child a.k.a managed */
		boot().childHandler(pipeApply(NettyPipe.Mode.DERIVED));

		activateFuture = boot().bind();

	}

	@Override
	protected void deactivateBoot() throws Exception {

		/** FIXME terminate children */

		deactivateFuture = channel().close();

		channel = null;
		boot = null;

	}

}
