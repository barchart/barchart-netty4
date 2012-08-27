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

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.barchart.netty.util.point.NetAddress;
import com.barchart.osgi.factory.api.FactoryDescriptor;

@Component(factory = DotMulticast.FACTORY)
public class DotMulticast extends DotBase {

	public static final String FACTORY = "barchart.netty.dot.multicast";

	@FactoryDescriptor
	private static final Map<String, String> descriptor;
	static {
		descriptor = new HashMap<String, String>();
		descriptor.put(PROP_FACTORY_ID, FACTORY);
		descriptor.put(PROP_FACTORY_DESCRIPTION,
				"multicast reader end point service");
	}

	/** multicast reader group address */
	protected NetAddress getGroupAddress() {
		return getNetPoint().getRemoteAddress();
	}

	/** default bind address */
	protected NetAddress getBindAddress() {
		return getNetPoint().getLocalAddress();
	}

	private Bootstrap boot;

	private NioDatagramChannel channel;

	protected NioDatagramChannel getChannel() {
		return channel;
	}

	protected ChannelInitializer<DatagramChannel> getHandler() {
		return new ChannelInitializer<DatagramChannel>() {
			@Override
			public void initChannel(final DatagramChannel ch) throws Exception {
				ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
			}
		};
	}

	@Override
	@Activate
	public void activate(final Map<String, String> props) {

		super.activate(props);

		channel = new NioDatagramChannel();

		boot = new Bootstrap();

		boot.localAddress(getNetPoint().getLocalAddress());

		boot.remoteAddress(getNetPoint().getRemoteAddress());

		boot.option(ChannelOption.IP_MULTICAST_TTL, getNetPoint()
				.getPacketTTL());

		boot.option(ChannelOption.IP_MULTICAST_ADDR, getNetPoint()
				.getRemoteAddress().getAddress());

		boot.group(getGroup());

		boot.channel(getChannel());

		boot.handler(getHandler());

		boot.bind();

	}

	@Override
	@Deactivate
	public void deactivate(final Map<String, String> props) {

		channel.close();
		channel = null;

		boot = null;

		super.deactivate(props);

	}

}
