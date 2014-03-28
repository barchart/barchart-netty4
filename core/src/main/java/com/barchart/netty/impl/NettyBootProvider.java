/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.impl;

import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.api.NettyBoot;
import com.barchart.netty.api.NettyBootManager;
import com.barchart.util.common.collections.BlockingConcurrentHashMap;

/** bootstrap collector */
@Component(immediate = true)
public class NettyBootProvider implements NettyBootManager {

	static {
		new NettySetup();
	}

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final BlockingConcurrentHashMap<String, NettyBoot> bootMap = //
			new BlockingConcurrentHashMap<String, NettyBoot>();

	@Override
	public NettyBoot findBoot(final String bootName) {
		if (bootName == null) {
			return null;
		}
		return bootMap.get(bootName);
	}

	@Override
	public NettyBoot findBoot(final String bootName, final long timeout,
			final TimeUnit unit) throws InterruptedException {
		if (bootName == null) {
			return null;
		}
		return bootMap.get(bootName, timeout, unit);
	}

	@Reference( //
	policy = ReferencePolicy.DYNAMIC, //
	cardinality = ReferenceCardinality.MULTIPLE //
	)
	protected void bind(final NettyBoot boot) {

		bootMap.put(boot.type(), boot);

		log.debug("@@@ boot-bind : {}", boot.type());

	}

	protected void unbind(final NettyBoot boot) {

		bootMap.remove(boot.type());

		log.debug("@@@ boot-unbind : {}", boot.type());

	}

}
