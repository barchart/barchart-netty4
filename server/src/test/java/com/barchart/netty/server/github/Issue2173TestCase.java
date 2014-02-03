package com.barchart.netty.server.github;

import java.net.URI;

/**
 * Test case for https://github.com/netty/netty/issues/2173
 */
public class Issue2173TestCase {

	public static void main(final String... args) throws Exception {

		new WebSocketServer().listen("localhost", 8888, "/test").sync();

		final WebSocketClient client = new WebSocketClient();
		while (!client.failed) {
			client.connect(URI.create("ws://localhost:8888/test")).channel().closeFuture().sync();
			Thread.sleep(1000);
		}

	}

}