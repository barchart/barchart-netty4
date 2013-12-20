package com.barchart.netty.client.protobuf;

import io.netty.buffer.MessageBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import com.barchart.account.api.AuthResult;
import com.barchart.account.common.request.AuthRequest;
import com.barchart.netty.client.messages.Capabilities;
import com.barchart.netty.client.messages.Ping;
import com.barchart.netty.client.messages.Pong;
import com.barchart.netty.client.messages.StartTLS;
import com.barchart.netty.client.messages.StopTLS;

@Sharable
public class BasicOpenfeedCodec extends MessageToMessageCodec<Object, Object> {

	@Override
	protected void encode(final ChannelHandlerContext ctx, final Object msg,
			final MessageBuf<Object> out) throws Exception {

		if (msg instanceof Capabilities) {
		} else if (msg instanceof Ping) {
		} else if (msg instanceof Pong) {
		} else if (msg instanceof StartTLS) {
		} else if (msg instanceof StopTLS) {
		} else if (msg instanceof AuthResult) {
		} else if (msg instanceof AuthRequest) {
		} else {
			out.add(msg);
		}

	}

	@Override
	protected void decode(final ChannelHandlerContext ctx, final Object msg,
			final MessageBuf<Object> out) throws Exception {

		out.add(msg);

	}

}