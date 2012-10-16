package com.barchart.netty.host.api;

import io.netty.channel.Channel;

import com.barchart.netty.util.point.NetPoint;

/** represents netty pipeline builder */
public interface NettyPipe {

	enum Mode {

		/** default or parent */
		DEFAULT, //

		/** derived or child */
		DERIVED, //

	}

	/** UUID of this pipeline builder */
	String type();

	/**
	 * build a new pipeline and apply it to the channel
	 * 
	 * @param netPoint
	 *            TODO
	 */
	void apply(NetPoint netPoint, Channel channel, Mode mode);

}
