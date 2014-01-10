package com.barchart.netty.server.base;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import com.barchart.netty.common.PipelineInitializer;
import com.barchart.netty.server.Server;
import com.barchart.netty.server.ServerBuilder;

public abstract class AbstractServerBuilder<S extends Server<S>, T extends AbstractBootstrap<T, ?>, B extends AbstractServerBuilder<S, T, B>>
		implements ServerBuilder<S, T, B> {

	protected EventLoopGroup defaultGroup = new NioEventLoopGroup();
	protected PipelineInitializer pipelineInit = null;
	protected BootstrapInitializer<T> bootstrapInit = null;

	@SuppressWarnings("unchecked")
	@Override
	public B group(final EventLoopGroup group) {
		defaultGroup = group;
		return (B) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public B pipeline(final PipelineInitializer inititalizer) {
		pipelineInit = inititalizer;
		return (B) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public B bootstrapper(final BootstrapInitializer<T> inititalizer) {
		bootstrapInit = inititalizer;
		return (B) this;
	}

}
