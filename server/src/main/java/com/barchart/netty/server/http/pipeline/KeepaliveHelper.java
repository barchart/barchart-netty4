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
