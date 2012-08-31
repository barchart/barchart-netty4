package com.barchart.netty.part.dot;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

import com.barchart.osgi.factory.api.FactoryDescriptor;

/**
 * parent for connection oriented end point servers
 * 
 * such as tcp, sctp
 */
@Component(factory = DotStreamServer.FACTORY)
public class DotStreamServer extends DotStream {

	public static final String FACTORY = "barchart.netty.dot.stream.server";

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
				"unicast reader/writer end point service");
	}

	private ServerBootstrap boot;

	protected ServerBootstrap boot() {
		return boot;
	}

	private NioServerSocketChannel channel;

	protected NioServerSocketChannel channel() {
		return channel;
	}

	protected ChannelFuture activateFuture;
	protected ChannelFuture deactivateFuture;

	@Override
	protected void activateBoot() throws Exception {

		boot = new ServerBootstrap();
		channel = new NioServerSocketChannel();

		boot().localAddress(localAddress());

		boot().option(ChannelOption.SO_SNDBUF,
				getNetPoint().getSendBufferSize());
		boot().option(ChannelOption.SO_RCVBUF,
				getNetPoint().getReceiveBufferSize());

		boot().childOption(ChannelOption.SO_SNDBUF,
				getNetPoint().getSendBufferSize());
		boot().childOption(ChannelOption.SO_RCVBUF,
				getNetPoint().getReceiveBufferSize());

		boot().group(group());

		boot().channel(channel());

		/** acceptor aka server aka parent */
		boot().handler(handler(pipeline()));

		/** connector aka client aka child */
		boot().childHandler(handler(managedPipeline()));

		activateFuture = boot().bind();

	}

	@Override
	protected void deactivateBoot() throws Exception {

		/** FIXME terminate children */

		deactivateFuture = channel().close();

		channel = null;
		boot = null;

	}

}
