/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.common.pipeline;

import io.netty.channel.ChannelPipeline;

/**
 * Base interface for objects that are interested in adding handlers to a
 * ChannelPipeline in preparation for communication to a remote host.
 */
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
