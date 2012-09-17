package com.barchart.netty.test.echo_msg;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.part.hand.SctpMessageCodec;
import com.barchart.netty.part.pipe.PipeAny;

/**  */
@Component(name = PipeEchoMsgClient.NAME, immediate = true)
public class PipeEchoMsgClient extends PipeAny {

	public static final String NAME = "barchart.netty.pipe.echo.message.client";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void apply(final NettyDot dot, final Channel channel) {

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("logger", new LoggingHandler());

		pipeline.addLast("sctp-codec", new SctpMessageCodec());

		pipeline.addLast("echo-client", new HandEchoMsgClient(128));

		log.debug("apply client : {}", channel);

	}

}
