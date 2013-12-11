package com.barchart.netty.client;

import io.netty.channel.ChannelPipeline;

public interface PipelineInitializer {

	/**
	 * Setup the channel pipeline on connect. An internal message router will be
	 * always appended to the end of the pipeline to properly handle receive()
	 * calls, so it is recommended that subclasses only add codecs to the
	 * pipeline and allow NettyClientBase to handle routing the resulting
	 * messages.
	 */
	public void initPipeline(final ChannelPipeline pipeline) throws Exception;

}
