package com.barchart.netty.host.api;

import aQute.bnd.annotation.ProviderType;

import com.barchart.osgi.factory.api.Cidget;

/** shared constants */
@ProviderType
public interface NettyAny extends Cidget {

	/* props */

	/** human readable description of the factory */
	String PROP_FACTORY_DESCRIPTION = "factory.description";

	/* */

}
