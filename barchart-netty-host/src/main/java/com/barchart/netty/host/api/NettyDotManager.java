package com.barchart.netty.host.api;

import com.barchart.osgi.factory.api.CidgetManager;
import com.typesafe.config.Config;

/** dot (end point) factory manager */
public interface NettyDotManager extends CidgetManager<NettyDot> {

	/**
	 * create a new dot form a properly formed hocon end point config entry
	 * 
	 * see net-point reference.conf for entry format
	 */
	NettyDot create(Config config);

	/***/
	boolean destroy(Config config);

	/***/
	boolean update(Config config);

}
