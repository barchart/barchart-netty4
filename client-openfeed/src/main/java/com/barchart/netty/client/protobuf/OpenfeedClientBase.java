package com.barchart.netty.client.protobuf;

import io.netty.channel.ChannelPipeline;

import com.barchart.account.api.Account;
import com.barchart.netty.client.base.AuthenticatingConnectableBase;
import com.barchart.netty.client.facets.SecureAware;
import com.barchart.netty.client.transport.TransportProtocol;

public class OpenfeedClientBase<T extends OpenfeedClientBase<T>> extends
		AuthenticatingConnectableBase<T, Account> {

	protected abstract static class Builder<B extends Builder<B, C>, C extends OpenfeedClientBase<C>>
			extends AuthenticatingConnectableBase.Builder<B, C, Account> {

		@SuppressWarnings("unchecked")
		public B credentials(final String username, final char[] password,
				final String deviceId, final String source) {

			authenticator(new PasswordAuthFlowHandler.Builder(username,
					password, deviceId, source));

			return (B) this;

		}

		@Override
		protected C configure(final C client) {
			security(SecureAware.Encryption.REFUSE);
			return super.configure(client);
		}

	}

	private final BasicOpenfeedPipeline basicPipeline;

	protected OpenfeedClientBase(final TransportProtocol transport_) {

		super(transport_);

		basicPipeline = new BasicOpenfeedPipeline();
	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {
		basicPipeline.initPipeline(pipeline);
		super.initPipeline(pipeline);
	}

}
