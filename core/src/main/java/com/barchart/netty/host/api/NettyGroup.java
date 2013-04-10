package com.barchart.netty.host.api;

import io.netty.channel.EventLoopGroup;
import aQute.bnd.annotation.ProviderType;

/** represents netty thread pool */
@ProviderType
public interface NettyGroup {

	EventLoopGroup getGroup();

}
