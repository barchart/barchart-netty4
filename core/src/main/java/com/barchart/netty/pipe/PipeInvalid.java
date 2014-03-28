/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.pipe;

import io.netty.channel.Channel;

import org.osgi.service.component.annotations.Component;


/** empty pipe */
@Component(name = PipeInvalid.TYPE, immediate = true)
public class PipeInvalid extends PipeAny {

	public static final String TYPE = "barchart.netty.pipe.invalid";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	protected void applyDefault(final Channel channel) {

		log.debug("apply default : {}", channel);

	}

	@Override
	protected void applyDerived(final Channel channel) {

		log.debug("apply derived : {}", channel);

	}

}
