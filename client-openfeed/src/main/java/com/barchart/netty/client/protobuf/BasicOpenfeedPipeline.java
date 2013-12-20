package com.barchart.netty.client.protobuf;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import com.barchart.netty.client.PipelineInitializer;

public class BasicOpenfeedPipeline implements PipelineInitializer {

	public BasicOpenfeedPipeline() {

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

		// TODO Decode into correct protobuf message here

		// Decode protobuf representations into POJOs (optional)
		pipeline.addLast("basic-codec", new BasicOpenfeedCodec());

	}

}
