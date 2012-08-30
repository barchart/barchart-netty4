package com.barchart.netty.host.api;

import com.barchart.osgi.factory.api.Fidget;

/** shared constants */
public interface NettyAny extends Fidget {

	/* props */

	/** factory which created this component */
	String PROP_FACTORY_ID = "factory-id";

	/** human readable description of the factory */
	String PROP_FACTORY_DESCRIPTION = "factory-description";

	/* */

	/** UUID of the factory that made this instance */
	String getFactoryId();

}
