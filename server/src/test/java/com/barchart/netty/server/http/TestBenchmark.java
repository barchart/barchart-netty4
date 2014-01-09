/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http;

import io.netty.channel.nio.NioEventLoopGroup;

import java.io.IOException;

import com.barchart.netty.server.Servers;
import com.barchart.netty.server.http.request.RequestHandlerBase;
import com.barchart.netty.server.http.request.ServerRequest;
import com.barchart.netty.server.http.request.ServerResponse;

public class TestBenchmark {

	public static void main(final String[] args) {

		final HttpServer server =
				Servers.createHttpServer()
						.requestHandler("", new TestRequestHandler())
						.parentGroup(new NioEventLoopGroup())
						.childGroup(new NioEventLoopGroup()).maxConnections(-1)
						.listen(8080);

		try {
			server.shutdownFuture().sync();
		} catch (final InterruptedException e) {
		}

	}

	private static class TestRequestHandler extends RequestHandlerBase {

		@Override
		public void onRequest(final ServerRequest request,
				final ServerResponse response) throws IOException {
			response.write("testing");
		}

	}

}
