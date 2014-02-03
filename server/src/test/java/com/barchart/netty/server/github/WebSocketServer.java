package com.barchart.netty.server.github;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

public class WebSocketServer {

	public static void main(final String... args) throws Exception {
		final WebSocketServer server = new WebSocketServer();
		server.listen("localhost", 8888, "/test");
	}

	public ChannelFuture listen(final String host, final int port, final String path) {

		return new ServerBootstrap()
				.channel(NioServerSocketChannel.class)
				.group(new NioEventLoopGroup())
				.childHandler(new ChannelInitializer<Channel>() {
					@Override
					protected void initChannel(final Channel ch) throws Exception {
						ch.pipeline().addLast(
								new HttpResponseEncoder(),
								new HttpRequestDecoder(),
								new HttpObjectAggregator(65536),
								new WebSocketServerProtocolHandler(path),
								new CapabilitiesBroadcaster());
					}
				}).bind(host, port);

	}

	private static class CapabilitiesBroadcaster extends ChannelInboundHandlerAdapter {

		@Override
		public void userEventTriggered(final ChannelHandlerContext ctx,
				final Object evt) throws Exception {

			if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
				ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer("Initial message".getBytes())));
			}

			ctx.fireUserEventTriggered(evt);

		}

	}

}
