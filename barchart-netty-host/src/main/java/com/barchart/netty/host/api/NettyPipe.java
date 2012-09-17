package com.barchart.netty.host.api;

import io.netty.channel.Channel;

/** represents netty pipeline builder */
public interface NettyPipe {

	enum Mode {

		/** default or parent */
		DEFAULT, //

		/** derived or child */
		DERIVED, //

	}

	/** UUID of this pipeline builder */
	String getName();

	/**
	 * build a new pipeline and apply it to the channel
	 */
	void apply(NettyDot dot, Channel channel, Mode mode);

}
