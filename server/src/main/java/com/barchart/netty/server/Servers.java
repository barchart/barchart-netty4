/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
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

	/**
	 * Create a new streaming TCP server. A pipeline initializer must be
	 * provided before starting the server.
	 */
	public static TCPServer createTCPServer() {
		return new TCPServer();
	}

	/**
	 * Create a new streaming UDT server. A pipeline initializer must be
	 * provided before starting the server.
	 */
	public static UDTServer createUDTServer() {
		return new UDTServer();
	}

	/**
	 * Create a new streaming SCTP server. A pipeline initializer must be
	 * provided before starting the server.
	 */
	public static SCTPServer createSCTPServer() {
		return new SCTPServer();
	}

	/**
	 * Create a new UDP unicast transceiver. A pipeline initializer must be
	 * provided before starting the server.
	 */
	public static UnicastTransceiver createUnicastTransceiver() {
		return new UnicastTransceiver();
	}

	/**
	 * Create a new UDP multicast transceiver. A pipeline initializer must be
	 * provided before starting the server.
	 */
	public static MulticastTransceiver createMulticastTransceiver() {
		return new MulticastTransceiver();
	}

	/**
	 * Create a new asynchronous HTTP server.
	 */
	public static HttpServer createHttpServer() {
		return new HttpServer();
	}

}
