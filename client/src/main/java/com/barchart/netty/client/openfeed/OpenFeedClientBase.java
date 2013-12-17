package com.barchart.netty.client.openfeed;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;

import java.net.URI;

import com.barchart.account.api.Account;
import com.barchart.netty.client.base.AuthenticatingConnectableBase;
import com.barchart.netty.client.transport.TransportProtocol;

public class OpenFeedClientBase<T extends OpenFeedClientBase<T>> extends
		AuthenticatingConnectableBase<T, Account> {

	protected abstract static class Builder<B extends Builder<B, C>, C extends OpenFeedClientBase<C>>
			extends AuthenticatingConnectableBase.Builder<B, C, Account> {

		/**
		 * Authenticate with the remote peer using the specified account and
		 * encryption key.
		 */
		@SuppressWarnings("unchecked")
		public B credentials(final URI account, final byte[] secretKey) {
			authenticator(new PrivateKeyAuthenticator(account, secretKey));
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B credentials(final URI account, final char[] secret) {
			authenticator(new PrivateKeyAuthenticator(account, secret));
			return (B) this;
		}

	}

	protected OpenFeedClientBase(final EventLoopGroup eventLoop_,
			final TransportProtocol transport_) {

		super(eventLoop_, transport_);

	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {

		// TODO Register codecs for messages based on OpenFeed header

	}

}
