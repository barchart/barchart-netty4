package com.barchart.netty.dot;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

import com.barchart.osgi.factory.api.FactoryDescriptor;

@Component(factory = DotUnicast.FACTORY)
public class DotUnicast extends DotBase {

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

	private NioDatagramChannel channel;

	protected NioDatagramChannel channel() {
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

	@Override
	protected void activateBoot() throws Exception {

		boot = new Bootstrap();
		channel = new NioDatagramChannel();

		boot().localAddress(getNetPoint().getLocalAddress());

		boot().remoteAddress(getNetPoint().getRemoteAddress());

		boot().option(ChannelOption.SO_REUSEADDR, true);

		boot().option(ChannelOption.IP_MULTICAST_TTL,
				getNetPoint().getPacketTTL());

		boot().group(group());

		boot().channel(channel());

		boot().handler(handler());

		boot().bind().sync();

	}

	@Override
	protected void deactivateBoot() throws Exception {

		channel().close().sync();

		channel = null;
		boot = null;

	}

}
