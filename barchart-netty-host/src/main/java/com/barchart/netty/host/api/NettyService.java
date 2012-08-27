package com.barchart.netty.host.api;

import com.barchart.netty.util.point.NetPoint;
import com.barchart.osgi.factory.api.Fidget;

/** end point channel */
public interface NettyService extends Fidget {

	NetPoint getPoint();

}
