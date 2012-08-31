package com.barchart.netty.host.impl;

import io.netty.logging.InternalLoggerFactory;
import io.netty.logging.Slf4JLoggerFactory;

class NettySetup {

	static {

		/**
		 * force ip4 to have correct multicast
		 * 
		 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6402758
		 */
		System.setProperty("java.net.preferIPv4Stack", "true");

		/**
		 * use slf4j for io.netty.logging.InternalLogger
		 */
		final InternalLoggerFactory defaultFactory = new Slf4JLoggerFactory();
		InternalLoggerFactory.setDefaultFactory(defaultFactory);

	}

}
