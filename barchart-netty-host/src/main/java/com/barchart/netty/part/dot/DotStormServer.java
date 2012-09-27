package com.barchart.netty.part.dot;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSctpServerChannel;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.barchart.netty.host.api.NettyPipe;

/**
 * parent for connection oriented server end points
 * 
 * such as SCTP
 */
@Component(name = DotStormServer.TYPE, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class DotStormServer extends DotAny {

	public static final String TYPE = "barchart.netty.dot.storm.server";

	private ServerBootstrap boot;

	protected ServerBootstrap boot() {
		return boot;
	}

	private NioSctpServerChannel channel;

	@Override
	public NioSctpServerChannel channel() {
		return channel;
	}

	protected ChannelFuture activateFuture;
	protected ChannelFuture deactivateFuture;

	@Override
	protected void bootActivate() throws Exception {

		boot = new ServerBootstrap();
		channel = new NioSctpServerChannel();

		boot().localAddress(localAddress());

		boot().option(ChannelOption.SO_BACKLOG, 100);

		/** https://github.com/netty/netty/issues/610 */
		// boot().option(ChannelOption.SO_SNDBUF,
		// getNetPoint().getSendBufferSize());
		// boot().option(ChannelOption.SO_RCVBUF,
		// getNetPoint().getReceiveBufferSize());

		boot().childOption(ChannelOption.SCTP_NODELAY, true);

		/** https://github.com/netty/netty/issues/610 */
		boot().childOption(ChannelOption.SO_SNDBUF,
				netPoint().getSendBufferSize());
		boot().childOption(ChannelOption.SO_RCVBUF,
				netPoint().getReceiveBufferSize());

		boot().group(group());

		boot().channelFactory(new FixedChannelFactory(channel()));

		/** acceptor a.k.a server a.k.a parent a.k.a default */
		boot().handler(pipeApply(NettyPipe.Mode.DEFAULT));

		/** connector a.k.a client a.k.a child a.k.a managed */
		boot().childHandler(pipeApply(NettyPipe.Mode.DERIVED));

		activateFuture = boot().bind();

	}

	@Override
	protected void bootDeactivate() throws Exception {

		/** FIXME terminate children */

		deactivateFuture = channel().close();

		channel = null;
		boot = null;

	}

}
