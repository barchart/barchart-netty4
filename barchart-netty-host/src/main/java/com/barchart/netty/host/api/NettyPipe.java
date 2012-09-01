package com.barchart.netty.host.api;

import io.netty.channel.Channel;

/** represents netty pipeline builder */
public interface NettyPipe {

	/** UUID of this pipeline builder */
	String getName();

	/**
	 * default / parent pipeline applicator;
	 * 
	 * build a new pipeline and apply it to the channel
	 */
	void apply(Channel channel);

	/**
	 * derived / managed pipeline applicator;
	 * 
	 * build a new pipeline and apply it to the channel
	 */
	void applyChild(Channel channel);

}
