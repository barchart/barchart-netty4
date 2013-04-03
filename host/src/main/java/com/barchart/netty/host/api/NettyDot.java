package com.barchart.netty.host.api;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import com.barchart.netty.util.point.NetPoint;

/**
 * represents netty "dot" - end point / channel
 * 
 * contract: net-point id defines dot id
 */
public interface NettyDot extends NettyAny {

	/* properties */

	// String PROP_NET_POINT = "net-point";

	/* attributes */

	AttributeKey<NetPoint> ATTR_NET_POINT //
	= new AttributeKey<NetPoint>("net-point");

	/* methods */

	/** net point that were used to configure this channel */
	NetPoint netPoint();

	/** transient/current netty channel associated with this dot */
	Channel channel();

}
