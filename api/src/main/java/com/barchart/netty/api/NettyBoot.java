package com.barchart.netty.api;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import aQute.bnd.annotation.ProviderType;

import com.barchart.netty.util.point.NetPoint;

/**
 * Netty "boot" - connection bootstrapper / channel creator.
 */
@ProviderType
public interface NettyBoot {

	/**
	 * UUID of this connection boostrapper
	 */
	String type();

	/**
	 * Initiate new connection defined by the NetPoint.
	 */
	public ChannelFuture startup(NetPoint netPoint) throws Exception;

	/**
	 * Terminate old connection defined by the NetPoint.
	 * <p>
	 * FIXME remove channel parameter.
	 */
	public ChannelFuture shutdown(NetPoint netPoint, Channel channel)
			throws Exception;

}
