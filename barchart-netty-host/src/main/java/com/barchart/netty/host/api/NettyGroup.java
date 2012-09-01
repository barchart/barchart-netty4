package com.barchart.netty.host.api;

import io.netty.channel.EventLoopGroup;

/** represents netty thread pool */
public interface NettyGroup {

	EventLoopGroup getGroup();

}
