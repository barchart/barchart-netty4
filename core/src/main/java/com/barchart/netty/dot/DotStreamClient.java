/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.dot;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.barchart.netty.api.NettyBoot;
import com.barchart.netty.boot.BootStreamClient;

/**
 * parent for connection oriented client end points
 * 
 * such as TCP
 */
@Component(name = DotStreamClient.TYPE, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class DotStreamClient extends DotAny {

	public static final String TYPE = "barchart.netty.dot.stream.client";

	@Override
	protected NettyBoot boot() {
		return bootManager().findBoot(BootStreamClient.TYPE);
	}

}
