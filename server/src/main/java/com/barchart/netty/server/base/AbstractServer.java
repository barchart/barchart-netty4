/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.base;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.barchart.netty.common.pipeline.PipelineInitializer;
import com.barchart.netty.server.Server;
import com.barchart.netty.server.ServerBuilder;

/**
 * Base abstract server that functions as its own builder to allow for runtime
 * configuration changes.
 */
public abstract class AbstractServer<T extends AbstractServer<T, B>, B extends AbstractBootstrap<B, ?>>
		implements Server<T>, ServerBuilder<T, B, T>, PipelineInitializer {

	protected final ChannelGroup serverChannels = new DefaultChannelGroup(
			GlobalEventExecutor.INSTANCE);

	protected final ChannelGroup clientChannels = new DefaultChannelGroup(
			GlobalEventExecutor.INSTANCE);

	private final ServerShutdownListener shutdownListener =
			new ServerShutdownListener();

	private final ClientTracker clientTracker = new ClientTracker();

	private DefaultPromise<T> shutdownFuture = null;
	protected EventLoopGroup defaultGroup = new NioEventLoopGroup();
	protected PipelineInitializer pipelineInit = null;
	protected BootstrapInitializer<B> bootstrapInit = null;

	@SuppressWarnings("unchecked")
	@Override
	public T group(final EventLoopGroup group) {
		defaultGroup = group;
		return (T) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T pipeline(final PipelineInitializer inititalizer) {
		pipelineInit = inititalizer;
		return (T) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T bootstrapper(final BootstrapInitializer<B> inititalizer) {
		bootstrapInit = inititalizer;
		return (T) this;
	}

	@Override
	public ChannelFuture listen(final int port, final String hostOrIp) {
		return listen(new InetSocketAddress(hostOrIp, port));
	}

	@Override
	public ChannelFuture listen(final SocketAddress address) {

		shutdownFuture = new DefaultPromise<T>(GlobalEventExecutor.INSTANCE);

		final ChannelFuture future = bootstrap().bind(address);

		serverChannels.add(future.channel());

		return future;

	}

	@Override
	public Future<T> shutdown() {

		if (serverChannels.size() == 0) {
			throw new IllegalStateException("Server is not running.");
		}

		final ChannelGroupFuture future = serverChannels.close();
		future.addListener(shutdownListener);

		return shutdownFuture;

	}

	@Override
	public Future<T> shutdownFuture() {
		return shutdownFuture;
	}

	@Override
	public Future<T> kill() {

		if (serverChannels.size() == 0) {
			throw new IllegalStateException("Server is not running.");
		}

		serverChannels.addAll(clientChannels);

		final ChannelGroupFuture future = serverChannels.close();
		future.addListener(shutdownListener);

		return shutdownFuture;

	}

	@Override
	public boolean running() {
		return serverChannels.size() > 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T build() {
		return (T) this;
	}

	protected int connections() {
		return clientChannels.size();
	}

	/**
	 * Default bootstrap for this server.
	 */
	protected abstract B bootstrap();

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {
		if (pipelineInit != null) {
			pipelineInit.initPipeline(pipeline);
		}
	}

	/**
	 * Default pipeline initializer.
	 */
	protected class ServerChannelInitializer extends
			ChannelInitializer<SocketChannel> {

		public ServerChannelInitializer() {
		}

		@Override
		public void initChannel(final SocketChannel ch) throws Exception {

			final ChannelPipeline pipeline = ch.pipeline();

			initPipeline(pipeline);

			pipeline.addLast(clientTracker);

		}

	}

	@Sharable
	private class ClientTracker extends ChannelInboundHandlerAdapter {

		@Override
		public void channelActive(final ChannelHandlerContext context) {
			clientChannels.add(context.channel());
			context.fireChannelActive();
		}

	}

	private class ServerShutdownListener implements
			GenericFutureListener<ChannelGroupFuture> {

		@SuppressWarnings("unchecked")
		@Override
		public void operationComplete(final ChannelGroupFuture future)
				throws Exception {
			try {
				future.get();
				shutdownFuture.setSuccess((T) AbstractServer.this);
			} catch (final Throwable t) {
				shutdownFuture.setFailure(t);
			}
		}

	}

}
