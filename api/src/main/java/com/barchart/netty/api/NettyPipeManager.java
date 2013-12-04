package com.barchart.netty.api;

import java.util.concurrent.TimeUnit;

import aQute.bnd.annotation.ProviderType;

/**
 * netty pipeline factory manager;
 * 
 * maintains registry of all present pipelines
 */
@ProviderType
public interface NettyPipeManager {

	/** @return valid pipe or null when not present */
	NettyPipe findPipe(String pipeName);

	/** @return valid pipe or null when not present */
	NettyPipe findPipe(String pipeName, long timeout, TimeUnit unit)
			throws InterruptedException;

}
