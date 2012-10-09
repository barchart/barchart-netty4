package com.barchart.netty.host.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.barchart.netty.host.api.NettyBoot;
import com.barchart.netty.host.api.NettyBootManager;

/** bootstrap collector */
@Component(immediate = true)
public class NettyBootProvider implements NettyBootManager {

	private final ConcurrentMap<String, NettyBoot> bootMap = //
	new ConcurrentHashMap<String, NettyBoot>();

	@Override
	public NettyBoot findBoot(final String bootName) {
		if (bootName == null) {
			return null;
		}
		return bootMap.get(bootName);
	}

	@Reference( //
	policy = ReferencePolicy.DYNAMIC, //
	cardinality = ReferenceCardinality.MULTIPLE //
	)
	protected void bind(final NettyBoot boot) {

		bootMap.put(boot.type(), boot);

	}

	protected void unbind(final NettyBoot boot) {

		bootMap.remove(boot.type());

	}

}
