package com.barchart.netty.server.github;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;
import java.net.URI;

public class WebSocketClient {

	public volatile boolean failed = false;

	public static void main(final String... args) throws Exception {
		final WebSocketClient client = new WebSocketClient();
		client.connect(URI.create("ws://localhost:8888/test"));
	}

	public ChannelFuture connect(final URI uri) {

		final NioEventLoopGroup elg = new NioEventLoopGroup();

		final ChannelFuture future = new Bootstrap()
				.channel(NioSocketChannel.class)
				.group(elg)
				.remoteAddress(new InetSocketAddress(uri.getHost(), uri.getPort()))
				.handler(new ChannelInitializer<Channel>() {
					@Override
					protected void initChannel(final Channel ch) throws Exception {
						ch.pipeline().addLast(
								new LoggingHandler("client"),
								new HttpClientCodec(),
								new ErrorReporter(),
								new HttpObjectAggregator(65536),
								new WebSocketClientProtocolHandler(
										WebSocketClientHandshakerFactory.newHandshaker(uri,
												WebSocketVersion.V13, null, false, null)),
								new CapabilitiesReceiver()
								);
					}
				}).connect();

		future.channel().closeFuture().addListener(new GenericFutureListener<Future<Void>>() {
			@Override
			public void operationComplete(final Future<Void> future) throws Exception {
				elg.shutdownGracefully();
			}
		});

		return future;

	}

	private class ErrorReporter extends ChannelInboundHandlerAdapter {

		@Override
		public void exceptionCaught(final ChannelHandlerContext ctx,
				final Throwable cause) throws Exception {
			failed = true;
			cause.printStackTrace();
			ctx.fireExceptionCaught(cause);
		}


	}

	private static class CapabilitiesReceiver extends ChannelInboundHandlerAdapter {

		@Override
		public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
			System.out.println("Received message: " + msg.toString());
			ctx.fireChannelRead(msg);
			ctx.close();
		}

		@Override
		public void userEventTriggered(final ChannelHandlerContext ctx,
				final Object evt) throws Exception {

			if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
				System.out.println("Client handshake complete");
			}

			ctx.fireUserEventTriggered(evt);

		}

	}

}
