package com.barchart.netty.host.api;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import com.barchart.netty.util.point.NetAddress;
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

	public static final AttributeKey<NetAddress> //
	ATTR_LOCAL_ADDRESS = new AttributeKey<NetAddress>("local-address");

	public static final AttributeKey<NetAddress> //
	ATTR_REMOTE_ADDRESS = new AttributeKey<NetAddress>("remote-address");

	/* methods */

	/** net point that were used to configure this channel */
	NetPoint getNetPoint();

	/** netty channel associated with this dot */
	Channel getChannel();

}
