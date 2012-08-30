package com.barchart.netty.host.api;

import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;

/** represents netty pipline (ordered list of handlers) */
public interface zzzNettyPipe extends NettyAny {

	/** handler : name -> instance */
	List<Map.Entry<String, NettyHand>> makePipe(Channel channel);

}
