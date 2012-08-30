package com.barchart.netty.host.api;

/** netty pipeline factory manager */
public interface NettyPipeManager {

	/** @return valid pipe or null */
	NettyPipe findPipe(String pipeName);

}
