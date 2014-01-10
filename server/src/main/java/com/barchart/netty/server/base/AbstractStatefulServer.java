package com.barchart.netty.server.base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * Abstract implementation of a stateful server over various protocols, with
 * separate channels for acceptor and child connections.
 */
public abstract class AbstractStatefulServer<T extends AbstractStatefulServer<T>>
		extends AbstractServer<T, ServerBootstrap> {

	protected Class<? extends ServerChannel> channelType =
			NioServerSocketChannel.class;
	protected EventLoopGroup childGroup = defaultGroup;

	@SuppressWarnings("unchecked")
	protected T channel(final Class<? extends ServerChannel> type) {
		channelType = type;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T childGroup(final EventLoopGroup group) {
		childGroup = group;
		return (T) this;
	}

	@Override
	protected ServerBootstrap bootstrap() {

		final ServerBootstrap bootstrap = new ServerBootstrap() //
				.group(defaultGroup, childGroup) //
				.channel(channelType) //
				.childHandler(new ServerChannelInitializer()) //
				.option(ChannelOption.SO_REUSEADDR, true) //
				.option(ChannelOption.SO_SNDBUF, 262144) //
				.option(ChannelOption.SO_RCVBUF, 262144) //
				.childOption(ChannelOption.SO_SNDBUF, 262144) //
				.childOption(ChannelOption.SO_RCVBUF, 262144);

		if (bootstrapInit != null) {
			bootstrapInit.initBootstrap(bootstrap);
		}

		return bootstrap;

	}

	public ChannelFuture listen(final int port) {
		return listen(new InetSocketAddress("0.0.0.0", port));
	}

}
