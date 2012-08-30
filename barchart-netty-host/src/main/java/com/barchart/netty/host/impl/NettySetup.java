package com.barchart.netty.host.impl;

import io.netty.logging.InternalLoggerFactory;
import io.netty.logging.Slf4JLoggerFactory;

class NettySetup {

	static {

		/** use slf4j for internal netty LoggingHandler */
		final InternalLoggerFactory defaultFactory = new Slf4JLoggerFactory();
		InternalLoggerFactory.setDefaultFactory(defaultFactory);

	}

}
