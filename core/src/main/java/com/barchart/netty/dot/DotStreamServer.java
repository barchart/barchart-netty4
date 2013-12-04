package com.barchart.netty.dot;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.barchart.netty.api.NettyBoot;
import com.barchart.netty.boot.BootStreamServer;

/**
 * parent for connection oriented server end points
 * 
 * such as TCP
 */
@Component(name = DotStreamServer.TYPE, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class DotStreamServer extends DotAny {

	public static final String TYPE = "barchart.netty.dot.stream.server";

	@Override
	protected NettyBoot boot() {
		return bootManager().findBoot(BootStreamServer.TYPE);
	}

}
