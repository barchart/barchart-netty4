package com.barchart.netty.server;

import com.barchart.netty.server.http.HttpServer;

public final class Servers {

	private Servers() {
	}

	public static HttpServer createHttpServer() {
		return new HttpServer();
	}

}
