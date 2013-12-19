package com.barchart.netty.client.protobuf;

import io.netty.channel.ChannelPipeline;

import org.openfeed.proto.generic.PacketType;

import com.barchart.account.api.Account;
import com.barchart.netty.client.base.AuthenticatingConnectableBase;
import com.barchart.netty.client.facets.SecureAware;
import com.barchart.netty.client.transport.TransportProtocol;
import com.google.protobuf.MessageLite;

public class ProtobufClientBase<T extends ProtobufClientBase<T>> extends
		AuthenticatingConnectableBase<T, Account> {

	protected abstract static class Builder<B extends Builder<B, C>, C extends ProtobufClientBase<C>>
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

	private final BasicProtobufPipeline basicPipeline;

	protected ProtobufClientBase(final TransportProtocol transport_) {

		super(transport_);

		basicPipeline = new BasicProtobufPipeline();
	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {
		basicPipeline.initPipeline(pipeline);
		super.initPipeline(pipeline);
	}

	/**
	 * Decode the body of the given Packet type as a specific protobuf message.
	 * 
	 * @param type The packet type
	 * @param message The protobuf subtype
	 */
	protected void codec(final PacketType type, final MessageLite message) {
		basicPipeline.codec(type, message);
	}

}
