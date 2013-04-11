package com.barchart.netty.dot;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.barchart.netty.api.NettyBoot;
import com.barchart.netty.boot.BootCast;

/**
 * parent for datagram based connection-less end points;
 * 
 * such as:
 * 
 * anycast, broadcast, unicast, multicast, etc;
 */
@Component(name = DotCast.TYPE, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class DotCast extends DotAny {

	public static final String TYPE = "barchart.netty.dot.cast";

	@Override
	protected NettyBoot boot() {
		return bootManager().findBoot(BootCast.TYPE);
	}

}
