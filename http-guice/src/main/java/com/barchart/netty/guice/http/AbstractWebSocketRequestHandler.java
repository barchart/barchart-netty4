package com.barchart.netty.guice.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;


/**
 * Injectable websocket service handler. This is a "throwaway handler" whose only purpose is to setup a pipeline for the
 * real processing when a websocket request comes in.
 */
public abstract class AbstractWebSocketRequestHandler implements WebSocketRequestHandler {

	/**
	 * Return the URI path that this websocket service should be registered at.
	 */
	@Override
	public abstract String path();

	@Override
	public abstract void initPipeline(ChannelPipeline pipeline) throws Exception;

	@Override
	public final void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
		initPipeline(ctx.pipeline());
		ctx.pipeline().remove(this);
	}

	@Override
	public final void handlerRemoved(final ChannelHandlerContext ctx) throws Exception {
	}

	@Override
	public final void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
		ctx.fireExceptionCaught(cause);
	}

}
