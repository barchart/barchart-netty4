/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.common.pipeline.PipelineInitializer;
import com.barchart.netty.server.Server;
import com.barchart.netty.server.ServerBuilder;
import com.barchart.netty.server.util.TimeoutPromise;

/**
 * Base abstract server that functions as its own builder to allow for runtime configuration changes.
 */
public abstract class AbstractServer<T extends AbstractServer<T, B>, B extends AbstractBootstrap<B, ?>>
		implements Server<T>, ServerBuilder<T, B, T>, PipelineInitializer {

	private final Logger log = LoggerFactory.getLogger(getClass());

	protected final DefaultChannelGroup serverChannels = new DefaultChannelGroup(
			GlobalEventExecutor.INSTANCE);

	protected final DefaultChannelGroup clientChannels = new DefaultChannelGroup(
			GlobalEventExecutor.INSTANCE);

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
		future.addListener(new ServerGroupCloseListener());

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
		future.addListener(new AllGroupCloseListener());

		return shutdownFuture;

	}

	protected Future<?> shutdownEventLoop() {
		return defaultGroup.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS);
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

		public ServerChannelInitializer() {}

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

	private class AllGroupCloseListener implements GenericFutureListener<Future<Void>> {

		@Override
		public void operationComplete(final Future<Void> future) throws Exception {

			log.debug("AllGroupCloseListener enter");
			try {
				future.get();
			} catch (final ExecutionException ee) {
				shutdownFuture.setFailure(ee.getCause());
			} catch (final Throwable t) {
				shutdownFuture.setFailure(t);
			}

			shutdownEventLoop().addListener(new EventLoopShutdownListener());

		}
	}

	private class ServerGroupCloseListener implements GenericFutureListener<Future<Void>> {

		@Override
		public void operationComplete(final Future<Void> future) throws Exception {

			log.debug("ServerGroupCloseListener enter");
			final AllGroupCloseListener cl = new AllGroupCloseListener();

			try {

				future.get();

				if (clientChannels.size() == 0) {

					log.debug("No clients");
					cl.operationComplete(future);

				} else {

					log.debug("Client shutdown, size=" + clientChannels.size());

					new TimeoutPromise(GlobalEventExecutor.INSTANCE, 2, TimeUnit.SECONDS, clientChannels.close())
							.addListener(cl);

					// final List<Future<?>> futures = new ArrayList<Future<?>>();
					//
					// for (final Channel c : clientChannels) {
					// futures.add(c.closeFuture());
					// }
					//
					// new TimeoutPromiseGroup(GlobalEventExecutor.INSTANCE, 2, TimeUnit.SECONDS, futures)
					// .addListener(cl);

				}

			} catch (final Throwable t) {
				log.debug("ServerGroupCloseListener exception", t);
				cl.operationComplete(future);
			}

		}

	}

	private class EventLoopShutdownListener implements GenericFutureListener<Future<Object>> {

		@SuppressWarnings("unchecked")
		@Override
		public void operationComplete(final Future<Object> future) throws Exception {

			// Previous shutdown errors may have completed the future, and now we're just cleaning up
			if (!shutdownFuture.isDone()) {

				try {
					future.get();
					shutdownFuture.setSuccess((T) AbstractServer.this);
				} catch (final ExecutionException ee) {
					shutdownFuture.setFailure(ee.getCause());
				} catch (final Throwable t) {
					shutdownFuture.setFailure(t);
				}

			}

		}

	}

}
