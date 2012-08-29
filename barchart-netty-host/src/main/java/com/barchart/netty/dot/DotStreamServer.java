package com.barchart.netty.dot;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.barchart.osgi.factory.api.FactoryDescriptor;

@Component(factory = DotStreamServer.FACTORY)
public class DotStreamServer extends DotBase {

	public static final String FACTORY = "barchart.netty.dot.stream.server";

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

	protected ChannelInitializer<DatagramChannel> handler() {
		return new ChannelInitializer<DatagramChannel>() {
			@Override
			public void initChannel(final DatagramChannel ch) throws Exception {
				ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
			}
		};
	}

	protected ChannelInitializer<DatagramChannel> handlerChild() {
		return new ChannelInitializer<DatagramChannel>() {
			@Override
			public void initChannel(final DatagramChannel ch) throws Exception {
				ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
			}
		};
	}

	@Override
	protected void activateBoot() throws Exception {

		boot().localAddress(getNetPoint().getLocalAddress());

		boot().group(group());

		boot().channel(channel());

		boot().handler(handler());

		boot().childHandler(handlerChild());

		boot().bind().sync();

	}

	@Override
	@Activate
	public void activate(final Map<String, String> props) throws Exception {

		super.activate(props);

		channel = new NioServerSocketChannel();
		boot = new ServerBootstrap();

		activateBoot();

	}

	@Override
	protected void deactivateBoot() throws Exception {

		channel().close().sync();

	}

	@Override
	@Deactivate
	public void deactivate(final Map<String, String> props) throws Exception {

		deactivateBoot();

		channel = null;
		boot = null;

		super.deactivate(props);

	}

}
