package com.barchart.netty.part.dot;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.barchart.netty.host.api.NettyBoot;
import com.barchart.netty.part.boot.BootStormClient;

/**
 * parent for connection oriented client end points
 * 
 * such as SCTP
 */
@Component(name = DotStormClient.TYPE, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class DotStormClient extends DotAny {

	public static final String TYPE = "barchart.netty.dot.storm.client";

	@Override
	protected NettyBoot boot() {
		return bootManager().findBoot(BootStormClient.TYPE);
	}

}
