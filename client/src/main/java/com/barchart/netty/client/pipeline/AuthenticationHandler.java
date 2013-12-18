package com.barchart.netty.client.pipeline;

import io.netty.channel.ChannelHandler;

import com.barchart.netty.client.facets.AuthenticationAware;

/**
 * Superclass for pluggable authentication handlers.
 * 
 * @see com.barchart.netty.client.facets.AuthenticationFacet
 * @see com.barchart.netty.client.base.AuthenticatingConnectableBase
 */
public interface AuthenticationHandler<A> extends AuthenticationAware<A>,
		ChannelHandler {

	interface Builder<B> {

		AuthenticationHandler<B> build();

	}

}
