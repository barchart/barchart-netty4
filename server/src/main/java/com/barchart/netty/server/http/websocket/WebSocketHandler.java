/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.websocket;

import com.barchart.netty.common.pipeline.PipelineInitializer;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Convenience class for wrapping an existing server pipeline initializer in a
 * ChannelHandler suitable for use as a websocket handler in HttpServer.
 * 
 * This assumes that the pipeline is using addLast() to add all handlers; if you
 * are doing any custom pipeline organization, you should wrap your own web
 * socket handler.
 * 
 * In particular, this means that wrapped pipelines must not include any TLS
 * negotiation, since websockets leaves that up to HTTP/SSL.
 */
public class WebSocketHandler extends ChannelHandlerAdapter {

	PipelineInitializer initializer;

	public WebSocketHandler(final PipelineInitializer initializer_) {
		initializer = initializer_;
	}

	@Override
	public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
		initializer.initPipeline(ctx.pipeline());
		ctx.pipeline().remove(this);
	}

}
