package com.barchart.netty.host.api;

import com.barchart.netty.util.point.NetPoint;

/** represents netty "dot" - end point / channel */
public interface NettyDot extends NettyAny {

	/* props */

	String PROP_NET_POINT_CONIFG = "net-point-config";

	/* */

	/** net point that were used to configure this channel */
	NetPoint getNetPoint();

}
