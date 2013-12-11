package com.barchart.netty.client.base;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.net.InetSocketAddress;

import org.openfeed.proto.generic.Packet;

import com.barchart.account.api.Account;
import com.barchart.netty.client.transport.TransportProtocol;

public class ProtobufClientBase<T extends ProtobufClientBase<T>> extends
		AuthenticatingConnectableBase<T, Account> {

	protected ProtobufClientBase(final EventLoopGroup eventLoop_,
			final InetSocketAddress address_, final TransportProtocol transport_) {

		super(eventLoop_, address_, transport_);

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

		// TODO Encode/decode Packets to their correct subtypes

	}

}
