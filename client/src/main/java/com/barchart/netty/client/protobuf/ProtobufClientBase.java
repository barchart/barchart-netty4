package com.barchart.netty.client.protobuf;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import org.openfeed.proto.generic.Packet;
import org.openfeed.proto.generic.PacketType;

import com.barchart.account.api.Account;
import com.barchart.netty.client.base.AuthenticatingConnectableBase;
import com.barchart.netty.client.transport.TransportProtocol;
import com.barchart.proto.buf.session.AuthRequestMessage;
import com.barchart.proto.buf.session.AuthResponseMessage;
import com.barchart.proto.buf.session.CapabilitiesMessage;
import com.barchart.proto.buf.session.SessionHeartbeatMessage;
import com.barchart.proto.buf.session.SessionPacketMessage;
import com.barchart.proto.buf.session.SessionTimestampMessage;
import com.google.protobuf.MessageLite;

public class ProtobufClientBase<T extends ProtobufClientBase<T>> extends
		AuthenticatingConnectableBase<T, Account> {

	protected abstract static class Builder<B extends Builder<B, C>, C extends ProtobufClientBase<C>>
			extends AuthenticatingConnectableBase.Builder<B, C, Account> {

		@SuppressWarnings("unchecked")
		public B credentials(final String username, final String password) {
			authenticator(new SessionAuthenticator(username, password));
			return (B) this;
		}

	}

	private final ProtobufPacketDecoder packetDecoder;
	private final ProtobufSessionDecoder sessionDecoder;

	protected ProtobufClientBase(final EventLoopGroup eventLoop_,
			final TransportProtocol transport_) {

		super(eventLoop_, transport_);

		packetDecoder = new ProtobufPacketDecoder();

		packetDecoder.decodeAs(PacketType.SESSION,
				SessionPacketMessage.getDefaultInstance());

		sessionDecoder = new ProtobufSessionDecoder();

		// Server capabilities - encryption / auth
		sessionDecoder.decodeAs(SessionPacketMessage.Type.CAPABILITIES,
				CapabilitiesMessage.getDefaultInstance());

		// Ping/pong
		sessionDecoder.decodeAs(SessionPacketMessage.Type.SESSION_HEARTBEAT,
				SessionHeartbeatMessage.getDefaultInstance());
		sessionDecoder.decodeAs(SessionPacketMessage.Type.TIMESTAMP,
				SessionTimestampMessage.getDefaultInstance());

		// Auth
		sessionDecoder.decodeAs(SessionPacketMessage.Type.AUTH_REQUEST,
				AuthRequestMessage.getDefaultInstance());
		sessionDecoder.decodeAs(SessionPacketMessage.Type.AUTH_RESPONSE,
				AuthResponseMessage.getDefaultInstance());

	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {

		// OUTBOUND

		pipeline.addLast("protobuf-framer",
				new ProtobufVarint32LengthFieldPrepender());
		pipeline.addLast("protobuf-encoder", new ProtobufEncoder());

		// INBOUND

		pipeline.addLast("protobuf-deframer",
				new ProtobufVarint32FrameDecoder());
		pipeline.addLast("protobuf-decoder",
				new ProtobufDecoder(Packet.getDefaultInstance()));

		// Decode base packets
		pipeline.addLast("protobuf-packet-decoder", packetDecoder);

		// Decode session packets
		pipeline.addLast("protobuf-session-decoder", sessionDecoder);

		// Decode protobuf representations into POJOs (optional)
		pipeline.addLast("basic-codec", new BasicProtobufCodec());

	}

	/**
	 * Decode the body of the given Packet type as a specific protobuf message.
	 * 
	 * @param type The packet type
	 * @param message The protobuf subtype
	 */
	protected void decodeAs(final PacketType type, final MessageLite message) {
		packetDecoder.decodeAs(type, message);
	}

}
