package com.barchart.netty.client.protobuf;

import io.netty.channel.ChannelHandler.Sharable;

import java.util.List;

import org.openfeed.proto.generic.Packet;
import org.openfeed.proto.generic.PacketType;

import com.google.protobuf.ByteString;

@Sharable
public class ProtobufPacketDecoder extends
		ProtobufSubtypeDecoder<Packet, PacketType> {

	public ProtobufPacketDecoder() {
		super(Packet.class);
	}

	@Override
	protected PacketType getSubType(final Packet packet) {
		return packet.getType();
	}

	@Override
	protected List<ByteString> getMessages(final Packet packet) {
		return packet.getBodyList();
	}

}
