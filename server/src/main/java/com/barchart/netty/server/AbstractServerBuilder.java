package com.barchart.netty.server;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public abstract class AbstractServerBuilder<S extends NettyServer, B extends AbstractServerBuilder<S, B>>
		implements ServerBuilder<S, B> {

	private final EventLoopGroup defaultGroup = new NioEventLoopGroup();

	protected int maxConnections = -1;
	protected EventLoopGroup parentGroup = defaultGroup;
	protected EventLoopGroup childGroup = defaultGroup;

	@SuppressWarnings("unchecked")
	@Override
	public B maxConnections(final int max) {
		maxConnections = max;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public B parentGroup(final EventLoopGroup group) {
		parentGroup = group;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public B childGroup(final EventLoopGroup group) {
		childGroup = group;
		return (B) this;
	}

	@Override
	public S listen(final int port) {
		return listen(new InetSocketAddress("0.0.0.0", port));
	}

	@Override
	public S listen(final int port, final String hostOrIp) {
		return listen(new InetSocketAddress(hostOrIp, port));
	}

	@Override
	public abstract S listen(final SocketAddress address);

}
