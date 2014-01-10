package com.barchart.netty.server.stream;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.barchart.netty.common.PipelineInitializer;
import com.barchart.netty.server.base.AbstractServer;
import com.barchart.netty.server.base.BootstrapInitializer;

public class UnicastTransceiver extends
		AbstractServer<UnicastTransceiver, Bootstrap> {

	protected PipelineInitializer pipelineInit = null;
	protected BootstrapInitializer<Bootstrap> bootstrapInit = null;
	protected InetSocketAddress remote;

	public UnicastTransceiver remote(final InetSocketAddress address) {
		remote = address;
		return this;
	}

	public UnicastTransceiver pipeline(final PipelineInitializer inititalizer) {
		pipelineInit = inititalizer;
		return this;
	}

	public UnicastTransceiver bootstrapper(
			final BootstrapInitializer<Bootstrap> inititalizer) {
		bootstrapInit = inititalizer;
		return this;
	}

	@Override
	protected Bootstrap bootstrap() {

		final Bootstrap bootstrap = new Bootstrap() //
				.group(defaultGroup) //
				.channel(NioDatagramChannel.class) //
				.handler(new ServerChannelInitializer()) //
				.remoteAddress(remote) //
				.option(ChannelOption.SO_REUSEADDR, true) //
				.option(ChannelOption.IP_MULTICAST_TTL, 3) //
				.option(ChannelOption.SO_SNDBUF, 262144) //
				.option(ChannelOption.SO_RCVBUF, 262144);

		if (bootstrapInit != null) {
			bootstrapInit.initBootstrap(bootstrap);
		}

		return bootstrap;

	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {
		pipelineInit.initPipeline(pipeline);
	}

	@Override
	public ChannelFuture listen(final SocketAddress address) {

		if (pipelineInit == null) {
			throw new IllegalStateException(
					"No pipeline initializer has been provided, server would do nothing");
		}

		return super.listen(address);

	}

}
