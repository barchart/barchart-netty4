package com.barchart.netty.host.api;

import com.barchart.netty.util.point.NetPoint;
import com.barchart.osgi.factory.api.Fidget;

/** end point channel */
public interface NettyDot extends Fidget {

	/* props */

	String PROP_FACTORY_ID = "factory-id";
	String PROP_FACTORY_DESCRIPTION = "factory-description";

	String PROP_NET_POINT_CONIFG = "net-point-config";

	/* */

	NetPoint getNetPoint();

}
