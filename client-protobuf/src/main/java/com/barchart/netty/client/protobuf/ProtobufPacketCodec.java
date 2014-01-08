package com.barchart.netty.client.protobuf;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openfeed.proto.generic.Packet;
import org.openfeed.proto.generic.PacketType;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

@Sharable
public class ProtobufPacketCodec extends
		MessageToMessageCodec<Packet, MessageLite> {

	private final Map<PacketType, MessageLite> codecs =
			new HashMap<PacketType, MessageLite>();

	private final Map<Class<? extends MessageLite>, PacketType> types =
			new HashMap<Class<? extends MessageLite>, PacketType>();

	public ProtobufPacketCodec() {
		super(Packet.class, MessageLite.class);
	}

	@Override
	protected void decode(final ChannelHandlerContext ctx, final Packet packet,
			final List<Object> out) throws Exception {

		if (codecs.containsKey(packet.getType())) {
			for (final ByteString body : packet.getBodyList()) {
				out.add(codecs.get(packet.getType()).getParserForType()
						.parseFrom(body));
			}
		} else {
			out.add(packet);
		}

	}

	@Override
	protected void encode(final ChannelHandlerContext ctx,
			final MessageLite msg, final List<Object> out) throws Exception {

		final PacketType type = types.get(msg.getClass());
		if (type != null) {
			out.add(Packet.newBuilder().setType(type)
					.addBody(msg.toByteString()));
		} else {
			out.add(msg);
		}

	}

	public void codec(final PacketType type, final MessageLite message) {
		codecs.put(type, message);
		types.put(message.getClass(), type);
	}

}
