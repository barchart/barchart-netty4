package com.barchart.netty.api;

import aQute.bnd.annotation.ProviderType;

import com.barchart.osgi.factory.api.CidgetManager;
import com.typesafe.config.Config;

/** dot (end point) factory manager */
@ProviderType
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
