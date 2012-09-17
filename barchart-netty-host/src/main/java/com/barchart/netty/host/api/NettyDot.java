package com.barchart.netty.host.api;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import com.barchart.netty.util.point.NetPoint;

/** represents netty "dot" - end point / channel */
public interface NettyDot extends NettyAny {

	/* properties */

	/**
	 * contract: dot factory must inject end point configuration as this
	 * property; see #NetPoint
	 */
	String PROP_NET_POINT = "net-point";

	/* attributes */

	AttributeKey<NettyDot> ATTR_NETTY_DOT //
	= new AttributeKey<NettyDot>("netty-dot");

	/* methods */

	/** net point that were used to configure this channel */
	NetPoint netPoint();

	/** netty channel associated with this dot */
	Channel channel();

}
