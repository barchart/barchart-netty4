package com.barchart.netty.client.protobuf;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import org.openfeed.proto.generic.Packet;
import org.openfeed.proto.generic.PacketType;

import com.barchart.netty.client.PipelineInitializer;
import com.barchart.proto.buf.session.SessionPacketMessage;
import com.google.protobuf.MessageLite;

public class BasicProtobufPipeline implements PipelineInitializer {

	private final ProtobufPacketCodec packetCodec;
	private final ProtobufSessionCodec sessionCodec;

	public BasicProtobufPipeline() {

		packetCodec = new ProtobufPacketCodec();

		packetCodec.codec(PacketType.SESSION,
				SessionPacketMessage.getDefaultInstance());

		sessionCodec = new ProtobufSessionCodec();

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
		pipeline.addLast("protobuf-packet-decoder", packetCodec);

		// Decode session packets
		pipeline.addLast("protobuf-session-decoder", sessionCodec);

		// Decode protobuf representations into POJOs (optional)
		pipeline.addLast("basic-codec", new BasicProtobufCodec());

	}

	/**
	 * Decode the body of the given Packet type as a specific protobuf message.
	 * 
	 * @param type The packet type
	 * @param message The protobuf subtype
	 */
	protected void codec(final PacketType type, final MessageLite message) {
		packetCodec.codec(type, message);
	}

}
