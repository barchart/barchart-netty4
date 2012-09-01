package com.barchart.netty.host.api;

import io.netty.channel.Channel;

/** represents netty pipeline (ordered list of handlers) */
public interface NettyPipe {

	/** UUID of this pipeline builder */
	String getName();

	/** build a new pipeline and apply it to the channel */
	void apply(Channel channel);

}
