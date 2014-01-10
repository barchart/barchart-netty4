/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
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
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

import com.barchart.netty.server.NettyServer;
import com.barchart.netty.server.http.pipeline.HttpRequestChannelHandler;

/**
 * High performance HTTP server.
 */
public class HttpServer extends NettyServer {

	private Channel serverChannel;
	private final HttpServerConfig config;
	private final HttpRequestChannelHandler channelHandler;
	private final ConnectionTracker clientTracker;

	private final ChannelGroup channelGroup = new DefaultChannelGroup(
			GlobalEventExecutor.INSTANCE);

	protected HttpServer(final HttpServerConfig config_) {
		config = config_;
		channelHandler = new HttpRequestChannelHandler(config);
		clientTracker = new ConnectionTracker(config.maxConnections());
	}

	/**
	 * Start the server with the configuration settings provided.
	 */
	public ChannelFuture listen() {

		if (config == null) {
			throw new IllegalStateException("Server has not been configured");
		}

		if (serverChannel != null) {
			throw new IllegalStateException("Server is already running.");
		}

		final ChannelFuture future = new ServerBootstrap() //
				.group(config.parentGroup(), config.childGroup()) //
				.channel(NioServerSocketChannel.class) //
				.localAddress(config.address()) //
				.childHandler(new HttpServerChannelInitializer()) //
				.option(ChannelOption.SO_REUSEADDR, true) //
				.option(ChannelOption.SO_SNDBUF, 262144) //
				.option(ChannelOption.SO_RCVBUF, 262144) //
				.bind();

		serverChannel = future.channel();

		return future;

	}

	/**
	 * Shutdown the server. This does not kill active client connections.
	 */
	public ChannelFuture shutdown() {

		if (serverChannel == null) {
			throw new IllegalStateException("Server is not running.");
		}

		final ChannelFuture future = serverChannel.close();
		serverChannel = null;

		return future;

	}

	/**
	 * Return a future for the server shutdown process.
	 */
	public ChannelFuture shutdownFuture() {
		return serverChannel.closeFuture();
	}

	/**
	 * Shutdown the server and kill all active client connections.
	 */
	public ChannelGroupFuture kill() {

		if (serverChannel == null) {
			throw new IllegalStateException("Server is not running.");
		}

		channelGroup.add(serverChannel);
		final ChannelGroupFuture future = channelGroup.close();
		channelGroup.remove(serverChannel);
		serverChannel = null;

		return future;

	}

	public boolean isRunning() {
		return serverChannel != null;
	}

	public HttpServerConfig config() {
		return config;
	}

	private class HttpServerChannelInitializer extends
			ChannelInitializer<SocketChannel> {

		@Override
		public void initChannel(final SocketChannel ch) throws Exception {

			final ChannelPipeline pipeline = ch.pipeline();

			pipeline.addLast(new HttpResponseEncoder(), //
					new ChunkedWriteHandler(), //
					clientTracker, //
					new HttpRequestDecoder(), //
					new HttpObjectAggregator(config.maxRequestSize()), //
					// new MessageLoggingHandler(LogLevel.INFO), //
					channelHandler);

		}

	}

	@Sharable
	private class ConnectionTracker extends ChannelInboundHandlerAdapter {

		private int maxConnections = -1;

		public ConnectionTracker(final int connections) {
			maxConnections = connections;
		}

		@Override
		public void channelActive(final ChannelHandlerContext context) {

			if (maxConnections > -1 && channelGroup.size() >= maxConnections) {

				final ByteBuf content = Unpooled.buffer();

				content.writeBytes("503 Service Unavailable - Server Too Busy"
						.getBytes());

				final FullHttpResponse response =
						new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
								HttpResponseStatus.SERVICE_UNAVAILABLE);

				response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,
						content.readableBytes());

				response.content().writeBytes(content);

				context.writeAndFlush(response).addListener(
						ChannelFutureListener.CLOSE);

				return;

			} else {

				channelGroup.add(context.channel());

			}

			context.fireChannelActive();

		}

		@Override
		public void channelInactive(final ChannelHandlerContext context) {

			channelGroup.remove(context.channel());
			context.fireChannelInactive();

		}

	}

}
