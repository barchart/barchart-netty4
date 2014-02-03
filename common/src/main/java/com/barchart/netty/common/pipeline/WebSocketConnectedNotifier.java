package com.barchart.netty.common.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Blocks downstream channelActive() notifications until a websocket handshake
 * completes.
 */
public class WebSocketConnectedNotifier extends
		ChannelInboundHandlerAdapter {

	List<Object> messages = new ArrayList<Object>();

	@Override
	public void userEventTriggered(final ChannelHandlerContext ctx,
			final Object evt) throws Exception {

		if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE ||
				evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {

			ctx.fireChannelActive();
			ctx.fireUserEventTriggered(evt);

			for (final Object msg : messages)
				ctx.fireChannelRead(msg);

			messages.clear();

			ctx.pipeline().remove(this);

		} else {

			ctx.fireUserEventTriggered(evt);

		}

	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx)
			throws Exception {
		// Block downstream relay until handshake completes
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
		// Queue inbound messages; sometimes the first messages can arrive
		// before the handshake complete event so downstream handlers haven't
		// reacted to channelActive() yet
		messages.add(msg);
	}

}