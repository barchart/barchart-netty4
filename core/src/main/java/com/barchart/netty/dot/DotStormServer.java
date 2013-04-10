package com.barchart.netty.dot;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.barchart.netty.api.NettyBoot;
import com.barchart.netty.boot.BootStormServer;

/**
 * parent for connection oriented server end points
 * 
 * such as SCTP
 */
@Component(name = DotStormServer.TYPE, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class DotStormServer extends DotAny {

	public static final String TYPE = "barchart.netty.dot.storm.server";

	@Override
	protected NettyBoot boot() {
		return bootManager().findBoot(BootStormServer.TYPE);
	}

}
