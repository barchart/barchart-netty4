package com.barchart.netty.part.dot;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.barchart.netty.host.api.NettyBoot;
import com.barchart.netty.part.boot.BootStreamClient;

/**
 * parent for connection oriented client end points
 * 
 * such as TCP
 */
@Component(name = DotStreamClient.TYPE, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class DotStreamClient extends DotAny {

	public static final String TYPE = "barchart.netty.dot.stream.client";

	@Override
	protected NettyBoot boot() {
		return bootManager().findBoot(BootStreamClient.TYPE);
	}

}
