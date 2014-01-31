package com.barchart.netty.common.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

/**
 * Codec that converts between ByteBuf and BinaryWebSocketFrame for transport
 * agnostic binary protocols.
 */
public class WebSocketBinaryCodec extends
		MessageToMessageCodec<BinaryWebSocketFrame, ByteBuf> {

	public WebSocketBinaryCodec() {
		super(BinaryWebSocketFrame.class, ByteBuf.class);
	}

	@Override
	protected void encode(final ChannelHandlerContext ctx,
			final ByteBuf msg, final List<Object> out) throws Exception {
		out.add(new BinaryWebSocketFrame(msg.retain()));
	}

	@Override
	protected void decode(final ChannelHandlerContext ctx,
			final BinaryWebSocketFrame msg, final List<Object> out)
			throws Exception {
		out.add(msg.content().retain());
	}

}