package com.barchart.netty.test.echo_byte;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.pipe.PipeAny;

/**  */
@Component(name = PipeEchoByteServer.TYPE, immediate = true)
public class PipeEchoByteServer extends PipeAny {

	public static final String TYPE = "barchart.netty.pipe.echo.byte.server";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	protected void applyDefault(final Channel channel) {

		log.debug("apply parent : {}", channel);

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("logger", new LoggingHandler());

	}

	@Override
	protected void applyDerived(final Channel channel) {

		log.debug("apply child : {}", channel);

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("logger", new LoggingHandler());

		pipeline.addLast("echo-server", new HandEchoByteServer());

	}

}
