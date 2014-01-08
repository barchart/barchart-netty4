package com.barchart.netty.client.protobuf;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

import com.barchart.proto.buf.session.AuthRequestMessage;
import com.barchart.proto.buf.session.AuthResponseMessage;
import com.barchart.proto.buf.session.CapabilitiesMessage;
import com.barchart.proto.buf.session.SessionHeartbeatMessage;
import com.barchart.proto.buf.session.SessionPacketMessage;
import com.barchart.proto.buf.session.SessionTimestampMessage;
import com.google.protobuf.MessageLite;

@Sharable
public class ProtobufSessionCodec extends
		MessageToMessageCodec<SessionPacketMessage, MessageLite> {

	public ProtobufSessionCodec() {
		super(SessionPacketMessage.class, MessageLite.class);
	}

	@Override
	protected void decode(final ChannelHandlerContext ctx,
			final SessionPacketMessage msg, final List<Object> out)
			throws Exception {

		switch (msg.getType()) {

			case CAPABILITIES:
				out.add(CapabilitiesMessage.parseFrom(msg.getBody()));
				break;

			case SESSION_HEARTBEAT:
				out.add(SessionHeartbeatMessage.parseFrom(msg.getBody()));
				break;

			case TIMESTAMP:
				out.add(SessionTimestampMessage.parseFrom(msg.getBody()));
				break;

			case AUTH_REQUEST:
				out.add(AuthRequestMessage.parseFrom(msg.getBody()));
				break;

			case AUTH_RESPONSE:
				out.add(AuthResponseMessage.parseFrom(msg.getBody()));
				break;

		}

	}

	@Override
	protected void encode(final ChannelHandlerContext ctx,
			final MessageLite msg, final List<Object> out) throws Exception {

		if (msg instanceof CapabilitiesMessage) {
			out.add(wrap(msg, SessionPacketMessage.Type.CAPABILITIES));
		} else if (msg instanceof SessionHeartbeatMessage) {
			out.add(wrap(msg, SessionPacketMessage.Type.SESSION_HEARTBEAT));
		} else if (msg instanceof SessionTimestampMessage) {
			out.add(wrap(msg, SessionPacketMessage.Type.TIMESTAMP));
		} else if (msg instanceof AuthRequestMessage) {
			out.add(wrap(msg, SessionPacketMessage.Type.AUTH_REQUEST));
		} else if (msg instanceof AuthResponseMessage) {
			out.add(wrap(msg, SessionPacketMessage.Type.AUTH_RESPONSE));
		} else {
			out.add(msg);
		}

	}

	private MessageLite wrap(final MessageLite msg,
			final SessionPacketMessage.Type type) {

		return SessionPacketMessage.newBuilder().setType(type)
				.setBody(msg.toByteString()).build();

	}

}
