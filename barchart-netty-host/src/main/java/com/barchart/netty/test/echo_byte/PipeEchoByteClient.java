package com.barchart.netty.test.echo_byte;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.part.pipe.PipeAny;

/**  */
@Component(name = PipeEchoByteClient.NAME, immediate = true)
public class PipeEchoByteClient extends PipeAny {

	public static final String NAME = "barchart.netty.pipe.echo.byte.client";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void apply(final NettyDot dot, final Channel channel) {

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("logger", new LoggingHandler());

		pipeline.addLast("echo-client", new HandEchoByteClient(128));

		log.debug("apply client : {}", channel);

	}

}
