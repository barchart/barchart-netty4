package com.barchart.netty.host.api;

import java.util.concurrent.TimeUnit;

/**
 * netty pipeline factory manager;
 * 
 * maintains registry of all present pipelines
 */
public interface NettyPipeManager {

	/** @return valid pipe or null when not present */
	NettyPipe findPipe(String pipeName);

	/** @return valid pipe or null when not present */
	NettyPipe findPipe(String pipeName, long timeout, TimeUnit unit)
			throws InterruptedException;

}
