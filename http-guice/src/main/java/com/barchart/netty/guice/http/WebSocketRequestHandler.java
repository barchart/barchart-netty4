package com.barchart.netty.guice.http;

import io.netty.channel.ChannelHandler;

import com.barchart.netty.common.pipeline.PipelineInitializer;

/**
 * Injectable web socket request handler.
 */
public interface WebSocketRequestHandler extends ChannelHandler, PipelineInitializer {

	/**
	 * Return the path that this handler should be registered at.
	 */
	public String path();

}
