package com.barchart.netty.server.http.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

public class WebSocketFrameUnpacker extends
		MessageToMessageDecoder<BinaryWebSocketFrame> {

	public WebSocketFrameUnpacker() {
	}

	@Override
	protected void decode(final ChannelHandlerContext ctx,
			final BinaryWebSocketFrame msg, final List<Object> out)
			throws Exception {
		msg.retain();
		out.add(msg.content());
	}

}