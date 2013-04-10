package com.barchart.netty.host.api;

import java.util.concurrent.TimeUnit;

import aQute.bnd.annotation.ProviderType;

/** boot (connection creator) factory manager */
@ProviderType
public interface NettyBootManager {

	/** @return valid bootstrap or null when not present */
	NettyBoot findBoot(String bootName);

	/** @return valid bootstrap or null when not present */
	NettyBoot findBoot(String bootName, long timeout, TimeUnit unit)
			throws InterruptedException;

}
