/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.barchart.netty.common.PipelineInitializer;
import com.barchart.netty.server.Server;

/**
 * High performance HTTP server.
 */
public abstract class AbstractServer<T extends AbstractServer<T>> extends
		AbstractServerBuilder<T, T> implements Server<T>, PipelineInitializer,
		BootstrapInitializer {

	private final ChannelGroup serverChannels = new DefaultChannelGroup(
			GlobalEventExecutor.INSTANCE);

	private final ChannelGroup clientChannels = new DefaultChannelGroup(
			GlobalEventExecutor.INSTANCE);

	private final DefaultPromise<T> shutdownFuture = new DefaultPromise<T>(
			GlobalEventExecutor.INSTANCE);

	private final ServerShutdownListener shutdownListener =
			new ServerShutdownListener();

	private final ClientTracker clientTracker = new ClientTracker();

	private ServerBootstrap bootstrap() {

		final ServerBootstrap bootstrap = new ServerBootstrap() //
				.group(parentGroup, childGroup) //
				.channel(channelType) //
				.childHandler(new ServerChannelInitializer()) //
				.option(ChannelOption.SO_REUSEADDR, true) //
				.option(ChannelOption.SO_SNDBUF, 262144) //
				.option(ChannelOption.SO_RCVBUF, 262144);

		initBootstrap(bootstrap);

		return bootstrap;

	}

	@Override
	public ChannelFuture listen(final int port) {
		return listen(new InetSocketAddress("0.0.0.0", port));
	}

	@Override
	public ChannelFuture listen(final int port, final String hostOrIp) {
		return listen(new InetSocketAddress(hostOrIp, port));
	}

	@Override
	public ChannelFuture listen(final SocketAddress address) {

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

	protected int connections() {
		return clientChannels.size();
	}

	/**
	 * Empty default implementation, override to customize bootstrap.
	 */
	@Override
	public void initBootstrap(final ServerBootstrap bootstrap) {
	}

	private class ServerChannelInitializer extends
			ChannelInitializer<SocketChannel> {

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
