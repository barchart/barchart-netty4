/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.concurrent.Callable;

import com.barchart.netty.server.util.ObjectPool;

/**
 * HTTP request/response object pool for low-garbage request handling
 */
public class HttpServerRequestPool {

	private final ObjectPool<PooledHttpServerRequest> requestPool;

	/**
	 * Create a new fixed-size message pool.
	 * 
	 * @param maxObjects_ The pool size, or -1 for unlimited
	 */
	public HttpServerRequestPool(final int maxObjects_) {

		requestPool =
				new ObjectPool<PooledHttpServerRequest>(maxObjects_,
						new Callable<PooledHttpServerRequest>() {
							@Override
							public PooledHttpServerRequest call()
									throws Exception {
								return new PooledHttpServerRequest();
							}
						});

	}

	/**
	 * Get an available request object, or null if none are available.
	 * 
	 * @return A pooled request object
	 */
	PooledHttpServerRequest provision(final ChannelHandlerContext context_,
			final FullHttpRequest nettyRequest_, final String relativeUri_,
			final KeepaliveHelper keepaliveHelper_) {

		return requestPool.poll().init(context_, nettyRequest_, relativeUri_,
				keepaliveHelper_);

	}

	void release(final PooledHttpServerRequest request) {

		requestPool.give(request.release());

	}

}
