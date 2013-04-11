package com.barchart.netty.api;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import aQute.bnd.annotation.ProviderType;

import com.barchart.netty.util.point.NetPoint;

/** represents netty "boot" - connection bootstrapper / channel creator */
@ProviderType
public interface NettyBoot {

	/** UUID of this connection boostrapper */
	String type();

	/** initiate new connection defined by the NetPoint **/
	public ChannelFuture startup(NetPoint netPoint) throws Exception;

	/** terminate old connection defined by the NetPoint **/
	public ChannelFuture shutdown(NetPoint netPoint, Channel channel)
			throws Exception;

}
