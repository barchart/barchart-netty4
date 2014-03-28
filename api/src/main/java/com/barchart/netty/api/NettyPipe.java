/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.api;

import io.netty.channel.Channel;
import aQute.bnd.annotation.ProviderType;

/** represents netty pipeline builder */
@ProviderType
public interface NettyPipe {

	enum Mode {

		/** default or parent */
		DEFAULT, //

		/** derived or child */
		DERIVED, //

	}

	/** UUID of this pipeline builder */
	String type();

	/**
	 * build a new pipeline and apply it to the channel
	 * <p>
	 * owner net-point will be attached as {@link NettyDot#ATTR_NET_POINT}
	 */
	void apply(Channel channel, Mode mode);

}
