package com.barchart.netty.part.dot;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.barchart.netty.host.api.NettyPipe;

/**
 * parent for connection oriented client end points
 * 
 * such as TCP
 */
@Component(name = DotStreamClient.TYPE, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class DotStreamClient extends DotAny {

	public static final String TYPE = "barchart.netty.dot.stream.client";

	private Bootstrap boot;

	protected Bootstrap boot() {
		return boot;
	}

	private NioSocketChannel channel;

	@Override
	public NioSocketChannel channel() {
		return channel;
	}

	protected ChannelFuture activateFuture;
	protected ChannelFuture deactivateFuture;

	@Override
	protected void bootActivate() throws Exception {

		boot = new Bootstrap();
		channel = new NioSocketChannel();

		boot().localAddress(localAddress());
		boot().remoteAddress(remoteAddress());

		boot().option(ChannelOption.SO_REUSEADDR, true);

		boot().option(ChannelOption.SO_SNDBUF, netPoint().getSendBufferSize());
		boot().option(ChannelOption.SO_RCVBUF,
				netPoint().getReceiveBufferSize());

		boot().group(group());

		boot().channelFactory(new FixedChannelFactory(channel()));

		/** connector */
		boot().handler(pipeApply(NettyPipe.Mode.DEFAULT));

		activateFuture = boot().connect();

	}

	@Override
	protected void bootDeactivate() throws Exception {

		deactivateFuture = channel().close();

		channel = null;
		boot = null;

	}

}
