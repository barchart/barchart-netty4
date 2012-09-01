package com.barchart.netty.host.api;

import io.netty.channel.Channel;

/** represents netty pipeline builder */
public interface NettyPipe {

	/** UUID of this pipeline builder */
	String getName();

	/**
	 * default / parent pipeline applicator;
	 * 
	 * build a new pipeline and apply it to the parent channel
	 */
	void apply(NettyDot dot, Channel channel);

	/**
	 * derived / managed pipeline applicator;
	 * 
	 * build a new pipeline and apply it to the child channel
	 */
	void applyChild(NettyDot dot, Channel channel);

}
