/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.api;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import aQute.bnd.annotation.ProviderType;

import com.barchart.netty.util.point.NetPoint;

/**
 * Netty "dot" - end point / channel.
 * <p>
 * Contract: {@link NetPoint} id defines dot id.
 */
@ProviderType
public interface NettyDot extends NettyAny {

	/* properties */

	/**
	 * Name of the net point property.
	 */
	String PROP_NET_POINT = "net-point";

	/* attributes */

	/**
	 * Channel attribute that stores the net point.
	 */
	AttributeKey<NetPoint> ATTR_NET_POINT //
	= new AttributeKey<NetPoint>(PROP_NET_POINT);

	/* methods */

	/**
	 * Net point that were used to configure this channel.
	 */
	NetPoint netPoint();

	/**
	 * Current netty channel associated with this dot.
	 */
	Channel channel();

}
