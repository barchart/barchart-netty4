package com.barchart.netty.test.echo_byte;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.pipe.PipeAny;

/**  */
@Component(name = PipeEchoByteClient.TYPE, immediate = true)
public class PipeEchoByteClient extends PipeAny {

	public static final String TYPE = "barchart.netty.pipe.echo.byte.client";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public void apply(final Channel channel, final Mode mode) {

		log.debug("apply client : {}", channel);

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("logger", new LoggingHandler());

		pipeline.addLast("echo-client", new HandEchoByteClient(128));

	}

}
