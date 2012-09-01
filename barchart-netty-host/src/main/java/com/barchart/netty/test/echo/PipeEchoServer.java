package com.barchart.netty.test.echo;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.part.pipe.PipeAny;

/**  */
@Component(name = PipeEchoServer.NAME, immediate = true)
public class PipeEchoServer extends PipeAny {

	public static final String NAME = "barchart.netty.pipe.echo.server";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void apply(final NettyDot dot, final Channel channel) {

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("logger", new LoggingHandler());

		log.debug("apply parent : {}", channel);

	}

	@Override
	public void applyChild(final NettyDot dot, final Channel channel) {

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("logger", new LoggingHandler());

		pipeline.addLast("echo-server", new HandEchoServer());

		log.debug("apply child : {}", channel);

	}

}
