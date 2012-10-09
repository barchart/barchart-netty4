package com.barchart.netty.host.api;

/** boot (connection creator) factory manager */
public interface NettyBootManager {

	/** @return valid bootstrap or null when not present */
	NettyBoot findBoot(String bootName);

}
