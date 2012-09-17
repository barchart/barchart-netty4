package com.barchart.netty.part.dot;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

/**
 * parent for connection oriented end points
 * 
 * such as tcp, sctp
 */
@Component(name = DotStream.FACTORY, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class DotStream extends DotAny {

	public static final String FACTORY = "barchart.netty.dot.stream";

	@Override
	public String factoryId() {
		return FACTORY;
	}

}
