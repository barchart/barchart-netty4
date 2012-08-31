package com.barchart.netty.part.dot;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

import com.barchart.osgi.factory.api.FactoryDescriptor;

/**
 * parent for datagram based connection-less end points;
 * 
 * such as:
 * 
 * anycast, broadcast, unicast, multicast, etc;
 */
@Component(factory = DotCast.FACTORY)
public class DotCast extends DotAny {

	public static final String FACTORY = "barchart.netty.dot.cast";

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
				"datagram reader/writer end point service");
	}

	private Bootstrap boot;

	protected Bootstrap boot() {
		return boot;
	}

	private NioDatagramChannel channel;

	protected NioDatagramChannel channel() {
		return channel;
	}

	@Override
	protected void activateBoot() throws Exception {

		boot = new Bootstrap();
		channel = new NioDatagramChannel();

		channel().attr(LOCAL_ADDRESS).set(localAddress());
		channel().attr(REMOTE_ADDRESS).set(remoteAddress());

		boot().localAddress(localAddress());

		boot().remoteAddress(remoteAddress());

		boot().option(ChannelOption.SO_REUSEADDR, true);

		boot().option(ChannelOption.IP_MULTICAST_TTL,
				getNetPoint().getPacketTTL());

		boot().group(group());

		boot().channel(channel());

		boot().handler(handler(pipeline()));

		boot().bind().sync();

	}

	@Override
	protected void deactivateBoot() throws Exception {

		channel().close().sync();

		channel = null;
		boot = null;

	}

}
