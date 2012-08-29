package com.barchart.netty.dot;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.barchart.osgi.factory.api.FactoryDescriptor;

@Component(factory = DotStreamClient.FACTORY)
public class DotStreamClient extends DotBase {

	public static final String FACTORY = "barchart.netty.dot.unicast";

	@FactoryDescriptor
	private static final Map<String, String> descriptor;
	static {
		descriptor = new HashMap<String, String>();
		descriptor.put(PROP_FACTORY_ID, FACTORY);
		descriptor.put(PROP_FACTORY_DESCRIPTION,
				"unicast reader/writer end point service");
	}

	private Bootstrap boot;

	protected Bootstrap boot() {
		return boot;
	}

	private NioSocketChannel channel;

	protected NioSocketChannel channel() {
		return channel;
	}

	protected ChannelInitializer<NioSocketChannel> handler() {
		return new ChannelInitializer<NioSocketChannel>() {
			@Override
			public void initChannel(final NioSocketChannel ch) throws Exception {
				ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
			}
		};
	}

	@Override
	protected void activateBoot() throws Exception {

		boot().localAddress(getNetPoint().getLocalAddress());

		boot().remoteAddress(getNetPoint().getRemoteAddress());

		boot().option(ChannelOption.SO_REUSEADDR, true);

		boot().group(group());

		boot().channel(channel());

		boot().handler(handler());

		boot().bind().sync();

	}

	@Override
	@Activate
	public void activate(final Map<String, String> props) throws Exception {

		super.activate(props);

		boot = new Bootstrap();
		channel = new NioSocketChannel();

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
