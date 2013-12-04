package com.barchart.netty.api;

import java.util.concurrent.TimeUnit;

import aQute.bnd.annotation.ProviderType;

/**
 * Boot (connection creator) factory manager.
 * <p>
 * FIXME change api.
 */
@ProviderType
public interface NettyBootManager {

	/**
	 * Discover existing bootstrap.
	 * 
	 * @return valid bootstrap or null when not present
	 */
	NettyBoot findBoot(String bootName);

	/**
	 * Discover existing bootstrap.
	 * 
	 * @return valid bootstrap or null when not present
	 */
	NettyBoot findBoot(String bootName, long timeout, TimeUnit unit)
			throws InterruptedException;

}
