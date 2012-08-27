package com.barchart.netty.dot;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

import com.barchart.osgi.factory.api.FactoryDescriptor;

@Component(factory = DotMulticast.FACTORY)
public class DotMulticast extends DotBase {

	public static final String FACTORY = "barchart.netty.dot.multicast";

	@FactoryDescriptor
	private static final Map<String, String> descriptor;
	static {
		descriptor = new HashMap<String, String>();
		descriptor.put(PROP_FACTORY_ID, FACTORY);
		descriptor.put(PROP_FACTORY_DESCRIPTION,
				"multicast reader end point service");
	}

}
