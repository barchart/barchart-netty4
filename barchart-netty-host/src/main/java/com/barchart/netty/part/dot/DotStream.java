package com.barchart.netty.part.dot;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

import com.barchart.osgi.factory.api.FactoryDescriptor;

/**
 * parent for connection oriented end points
 * 
 * such as tcp, sctp
 */
@Component(factory = DotStream.FACTORY)
public class DotStream extends DotAny {

	public static final String FACTORY = "barchart.netty.dot.stream";

	@Override
	public String getFactoryId() {
		return FACTORY;
	}

	@FactoryDescriptor
	private static final Map<String, String> descriptor;
	static {
		descriptor = new HashMap<String, String>();
		descriptor.put(PROP_FACTORY_ID, FACTORY);
		descriptor.put(PROP_FACTORY_DESCRIPTION,
				"stream reader/writer end point");
	}

}
