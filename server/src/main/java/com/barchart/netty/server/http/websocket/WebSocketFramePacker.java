package com.barchart.netty.server.http.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

public class WebSocketFramePacker extends MessageToMessageEncoder<ByteBuf> {

	@Override
	protected void encode(final ChannelHandlerContext ctx, final ByteBuf msg,
			final List<Object> out) throws Exception {
		final BinaryWebSocketFrame binaryWebSocketFrame =
				new BinaryWebSocketFrame(msg);
		ctx.writeAndFlush(binaryWebSocketFrame);
	}

}
