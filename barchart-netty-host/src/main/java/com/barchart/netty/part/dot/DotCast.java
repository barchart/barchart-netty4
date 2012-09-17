package com.barchart.netty.part.dot;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioDatagramChannel;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.barchart.netty.host.api.NettyPipe;

/**
 * parent for datagram based connection-less end points;
 * 
 * such as:
 * 
 * anycast, broadcast, unicast, multicast, etc;
 */
@Component(name = DotCast.FACTORY, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class DotCast extends DotAny {

	public static final String FACTORY = "barchart.netty.dot.cast";

	@Override
	public String factoryId() {
		return FACTORY;
	}

	private Bootstrap boot;

	protected Bootstrap boot() {
		return boot;
	}

	private NioDatagramChannel channel;

	@Override
	public NioDatagramChannel channel() {
		return channel;
	}

	@Override
	protected void activateBoot() throws Exception {

		boot = new Bootstrap();
		channel = new NioDatagramChannel();

		boot().localAddress(localAddress());
		boot().remoteAddress(remoteAddress());

		boot().option(ChannelOption.SO_SNDBUF, netPoint().getSendBufferSize());
		boot().option(ChannelOption.SO_RCVBUF,
				netPoint().getReceiveBufferSize());

		boot().option(ChannelOption.SO_REUSEADDR, true);

		boot().option(ChannelOption.IP_MULTICAST_TTL, netPoint().getPacketTTL());

		boot().group(group());

		boot().channelFactory(new FixedChannelFactory(channel()));

		boot().handler(pipeApply(NettyPipe.Mode.DEFAULT));

		boot().bind().sync();

	}

	@Override
	protected void deactivateBoot() throws Exception {

		channel().close().sync();

		channel = null;
		boot = null;

	}

}
