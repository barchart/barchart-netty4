package com.barchart.netty.part.dot;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

import com.barchart.osgi.factory.api.FactoryDescriptor;

/**
 * parent for connection oriented client end points
 * 
 * such as tcp, sctp
 */
@Component(factory = DotStreamClient.FACTORY)
public class DotStreamClient extends DotStream {

	public static final String FACTORY = "barchart.netty.dot.stream.client";

	@Override
	public String getFactoryId() {
		return FACTORY;
	}

	@FactoryDescriptor
	private static final Map<String, String> descriptor;
	static {
		descriptor = new HashMap<String, String>();
		descriptor.put(PROP_FACTORY_ID, FACTORY);
		descriptor.put(PROP_FACTORY_DESCRIPTION,
				"stream reader/writer end point client service");
	}

	private Bootstrap boot;

	protected Bootstrap boot() {
		return boot;
	}

	private NioSocketChannel channel;

	protected NioSocketChannel channel() {
		return channel;
	}

	protected ChannelFuture activateFuture;
	protected ChannelFuture deactivateFuture;

	@Override
	protected void activateBoot() throws Exception {

		boot = new Bootstrap();
		channel = new NioSocketChannel();

		boot().localAddress(localAddress());
		boot().remoteAddress(remoteAddress());

		boot().option(ChannelOption.SO_REUSEADDR, true);

		boot().option(ChannelOption.SO_SNDBUF,
				getNetPoint().getSendBufferSize());
		boot().option(ChannelOption.SO_RCVBUF,
				getNetPoint().getReceiveBufferSize());

		boot().group(group());

		boot().channel(channel());

		/** connector */
		boot().handler(pipeApply());

		activateFuture = boot().connect();

	}

	@Override
	protected void deactivateBoot() throws Exception {

		deactivateFuture = channel().close();

		channel = null;
		boot = null;

	}

}
