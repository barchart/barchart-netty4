package com.barchart.netty.server;

import com.barchart.netty.server.http.HttpServer;
import com.barchart.netty.server.stream.MulticastTransceiver;
import com.barchart.netty.server.stream.SCTPServer;
import com.barchart.netty.server.stream.TCPServer;
import com.barchart.netty.server.stream.UDTServer;
import com.barchart.netty.server.stream.UnicastTransceiver;

public final class Servers {

	private Servers() {
	}

	public static TCPServer createTCPServer() {
		return new TCPServer();
	}

	public static UDTServer createUDTServer() {
		return new UDTServer();
	}

	public static SCTPServer createSCTPServer() {
		return new SCTPServer();
	}

	public static UnicastTransceiver createUnicastTransceiver() {
		return new UnicastTransceiver();
	}

	public static MulticastTransceiver createMulticastTransceiver() {
		return new MulticastTransceiver();
	}

	public static HttpServer createHttpServer() {
		return new HttpServer();
	}

}
