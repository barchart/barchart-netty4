package com.barchart.netty.api;

import aQute.bnd.annotation.ProviderType;

import com.barchart.osgi.factory.api.Cidget;

/**
 * Constants shared by all netty components.
 */
@ProviderType
public interface NettyAny extends Cidget {

	/**
	 * Human readable description of the factory
	 */
	String PROP_FACTORY_DESCRIPTION = "factory.description";

}
