package com.barchart.netty.host.api;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import com.barchart.netty.util.point.NetPoint;

/** represents netty "boot" - connection bootstrapper / channel creator */
public interface NettyBoot {

	/** UUID of this connection boostrapper */
	String type();

	/** bootstrap a new connection defined by the NetPoint **/
	public ChannelFuture boot(NetPoint netPoint) throws Exception;

	/** bootstrap a new connection defined by the NetPoint **/
	public ChannelFuture shutdown(NetPoint netPoint, Channel channel)
			throws Exception;

}
