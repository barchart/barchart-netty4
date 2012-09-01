package com.barchart.netty.host.api;

import com.barchart.osgi.factory.api.FidgetManager;
import com.typesafe.config.Config;

/** dot (end point) factory manager */
public interface NettyDotManager extends FidgetManager<NettyDot> {

	/**
	 * create a new dot form a properly formed hocon end point config entry
	 */
	NettyDot create(Config config);

}
