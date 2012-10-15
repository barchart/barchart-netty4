package com.barchart.netty.part.dot;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.barchart.netty.host.api.NettyBoot;
import com.barchart.netty.part.boot.BootCast;

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
