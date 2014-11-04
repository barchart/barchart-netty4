/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.client.pipeline;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import com.barchart.netty.common.metadata.AuthenticationAware;

/**
 * Superclass for pluggable authentication handlers.
 *
 * @see com.barchart.netty.client.facets.AuthenticationFacet
 * @see com.barchart.netty.client.base.AuthenticatingConnectableBase
 */
public interface AuthenticationHandler<A> extends AuthenticationAware<A>, ChannelHandler {

	void authenticate(ChannelHandlerContext ctx);

	A response(Object ctx);

	interface Builder<B> {

		AuthenticationHandler<B> build();

	}

}
