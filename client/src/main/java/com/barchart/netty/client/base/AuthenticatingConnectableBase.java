/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.client.base;

import io.netty.channel.ChannelPipeline;
import rx.Observable;

import com.barchart.netty.client.facets.AuthenticationFacet;
import com.barchart.netty.client.pipeline.AuthenticationHandler;
import com.barchart.netty.client.transport.TransportProtocol;
import com.barchart.netty.common.metadata.AuthenticationAware;

/**
 * An authenticating Connectable proxy that authenticates a user with the host.
 * The actual authentication communication is implementation-dependent via a
 * provided AuthenticationHandler. MessageFlowHandler is a recommended base
 * class for writing authentication handlers in order to block downstream
 * handlers from receiving channelActive() events until authentication is
 * complete.
 * 
 * @param <A> The account object type
 */
public abstract class AuthenticatingConnectableBase<T extends AuthenticatingConnectableBase<T, A>, A>
		extends KeepaliveConnectableBase<T> implements AuthenticationAware<A> {

	protected abstract static class Builder<B extends Builder<B, C, D>, C extends AuthenticatingConnectableBase<C, D>, D>
			extends KeepaliveConnectableBase.Builder<B, C> {

		protected AuthenticationHandler.Builder<D> authenticatorBuilder;

		@SuppressWarnings("unchecked")
		public B authenticator(final AuthenticationHandler.Builder<D> builder_) {
			authenticatorBuilder = builder_;
			return (B) this;
		}

		@Override
		protected C configure(final C client) {
			super.configure(client);
			client.authFacet = new AuthenticationFacet<D>(authenticatorBuilder);
			return client;
		}

	}

	protected AuthenticationFacet<A> authFacet = null;

	protected AuthenticatingConnectableBase(final TransportProtocol transport_) {
		super(transport_);
	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {
		super.initPipeline(pipeline);
		authFacet.initPipeline(pipeline);
	}

	@Override
	public Observable<AuthState> authStateChanges() {
		return authFacet.authStateChanges();
	}

	@Override
	public AuthState authState() {
		return authFacet.authState();
	}

	@Override
	public A account() {
		return authFacet.account();
	}

}
