package com.barchart.netty.client.protobuf;

import io.netty.channel.ChannelHandler.Sharable;

import java.util.Arrays;
import java.util.List;

import com.barchart.proto.buf.session.SessionPacketMessage;
import com.google.protobuf.ByteString;

@Sharable
public class ProtobufSessionDecoder extends
		ProtobufSubtypeDecoder<SessionPacketMessage, SessionPacketMessage.Type> {

	public ProtobufSessionDecoder() {
		super(SessionPacketMessage.class);
	}

	@Override
	protected SessionPacketMessage.Type getSubType(
			final SessionPacketMessage packet) {
		return packet.getType();
	}

	@Override
	protected List<ByteString> getMessages(final SessionPacketMessage packet) {
		return Arrays.asList(packet.getBody());
	}

}
