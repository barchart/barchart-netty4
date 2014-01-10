package com.barchart.netty.server.base;

import com.barchart.netty.server.Server;
import com.barchart.netty.server.ServerBuilder;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public abstract class AbstractServerBuilder<S extends Server<S>, B extends AbstractServerBuilder<S, B>>
		implements ServerBuilder<S, B> {

	private final EventLoopGroup defaultGroup = new NioEventLoopGroup();

	protected EventLoopGroup parentGroup = defaultGroup;
	protected EventLoopGroup childGroup = defaultGroup;

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

}
