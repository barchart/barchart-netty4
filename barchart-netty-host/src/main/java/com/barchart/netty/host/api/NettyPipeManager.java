package com.barchart.netty.host.api;

/**
 * netty pipeline factory manager;
 * 
 * maintains registry of all present pipelines
 */
public interface NettyPipeManager {

	/** @return valid pipe or null when not present */
	NettyPipe findPipe(String pipeName);

}
