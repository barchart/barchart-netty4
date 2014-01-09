package com.barchart.netty.server;

import com.barchart.netty.server.http.HttpServerBuilder;

public final class Servers {

	private Servers() {
	}

	public static HttpServerBuilder createHttpServer() {
		return new HttpServerBuilder();
	}

}
