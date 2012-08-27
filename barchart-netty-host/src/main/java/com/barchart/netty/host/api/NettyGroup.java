package com.barchart.netty.host.api;

import io.netty.channel.EventLoopGroup;

/** shared thread pool */
public interface NettyGroup {

	EventLoopGroup getGroup();

}
