/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.IOException;

import com.barchart.netty.server.Servers;
import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.netty.server.http.request.RequestHandlerBase;

public class TestBenchmark {

	public static void main(final String[] args) {

		final HttpServer server =
				Servers.createHttpServer()
						.requestHandler("", new TestRequestHandler())
						.requestHandler("/test", new TestWebSocketPage())
						.webSocketHandler("/ws", new TestWebsocketHandler("1"))
						.webSocketHandler("/ws2", new TestWebsocketHandler("2"))
						.parentGroup(new NioEventLoopGroup())
						.childGroup(new NioEventLoopGroup()).maxConnections(-1);

		server.listen(8080);

		try {
			server.shutdownFuture().sync();
		} catch (final InterruptedException e) {
		}

	}

	private static class TestRequestHandler extends RequestHandlerBase {

		@Override
		public void handle(final HttpServerRequest request) throws IOException {
			request.response().write("testing");
			request.response().finish();
		}

	}

	private static class TestWebSocketPage extends RequestHandlerBase {

		@Override
		public void handle(final HttpServerRequest request) throws IOException {
			request.response()
					.write("<script language=\"javascript\">"
							+ "var ws = new WebSocket('ws://localhost:8080/ws');"
							+ "ws.onopen = function(evt) { console.log('1: ' + evt); };"
							+ "ws.onclose = function(evt) { console.log('1: ' + evt); };"
							+ "ws.onmessage = function(evt) { console.log('1: ' + evt); };"
							+ "ws.onerror = function(evt) { console.log('1: ' + evt); };"
							+ "var ws2 = new WebSocket('ws://localhost:8080/ws2');"
							+ "ws2.onopen = function(evt) { console.log('2: ' + evt); };"
							+ "ws2.onclose = function(evt) { console.log('2: ' + evt); };"
							+ "ws2.onmessage = function(evt) { console.log('2: ' + evt); };"
							+ "ws2.onerror = function(evt) { console.log('2: ' + evt); };"
							+ "</script>");
			request.response().finish();
		}

	}

	@Sharable
	private static class TestWebsocketHandler extends
			ChannelInboundHandlerAdapter {

		private final String label;

		public TestWebsocketHandler(final String label_) {
			label = label_;
		}

		@Override
		public void handlerAdded(final ChannelHandlerContext ctx)
				throws Exception {
			System.out.println(label + ": added");
		}

		@Override
		public void channelActive(final ChannelHandlerContext ctx)
				throws Exception {
			System.out.println(label + ": active");
			ctx.fireChannelActive();
		}

		@Override
		public void channelInactive(final ChannelHandlerContext ctx)
				throws Exception {
			System.out.println(label + ": inactive");
			ctx.fireChannelInactive();
		}

		@Override
		public void channelRead(final ChannelHandlerContext ctx,
				final Object msg) throws Exception {
			if (msg instanceof TextWebSocketFrame) {
				final TextWebSocketFrame tf = (TextWebSocketFrame) msg;
				final byte[] bytes = new byte[tf.content().readableBytes()];
				tf.content().readBytes(bytes);
				tf.content().resetReaderIndex();
				System.out.println(label + ": " + new String(bytes));
			}
		}
	}

}
