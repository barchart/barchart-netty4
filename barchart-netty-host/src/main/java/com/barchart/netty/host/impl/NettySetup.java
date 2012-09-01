package com.barchart.netty.host.impl;

import io.netty.logging.InternalLoggerFactory;
import io.netty.logging.Slf4JLoggerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** provides static initializers for netty */
class NettySetup {

	protected static final Logger log = LoggerFactory
			.getLogger(NettySetup.class);

	static final String PROP_IP4 = "java.net.preferIPv4Stack";

	/**
	 * force ip4 to have correct multicast
	 * 
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6402758
	 */
	static {

		System.setProperty(PROP_IP4, "true");

		log.info("system property : {}={}", PROP_IP4,
				System.getProperty(PROP_IP4));

	}

	/**
	 * use slf4j provider for io.netty.logging.InternalLogger
	 */
	static {

		final InternalLoggerFactory defaultFactory = new Slf4JLoggerFactory();

		InternalLoggerFactory.setDefaultFactory(defaultFactory);

		log.info("InternalLoggerFactory={}", InternalLoggerFactory
				.getDefaultFactory().getClass().getName());

	}

}
