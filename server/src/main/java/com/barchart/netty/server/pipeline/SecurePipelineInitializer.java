package com.barchart.netty.server.pipeline;

import io.netty.channel.ChannelPipeline;

import com.barchart.netty.common.pipeline.PipelineInitializer;

/**
 * Wrapper for a server pipeline that adds a StartTLS message responder for
 * activating TLS. This should always be separate from the core pipeline because
 * some transports already have their own encryption mechanisms (i.e. websockets
 * over HTTPS)
 */
public class SecurePipelineInitializer implements PipelineInitializer {

	private final PipelineInitializer initializer;

	/**
	 * Create a new secure pipeline initializer that wraps the given pipeline.
	 */
	public SecurePipelineInitializer(final PipelineInitializer initializer_) {
		initializer = initializer_;
	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {
		initializer.initPipeline(pipeline);
		pipeline.addLast(new StartTLSHandler());
	}

}