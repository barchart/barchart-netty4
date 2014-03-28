/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.pipeline;

import io.netty.channel.ChannelHandlerContext;

/**
 * Helper for handling keepalives with pooled requests objects.
 */
public interface KeepaliveHelper {

	/**
	 * Notify the server that the current request is finished, and can be
	 * cleaned up and returned to the pool.
	 */
	PooledHttpServerRequest requestComplete(ChannelHandlerContext ctx);

}
