package com.barchart.netty.test.arb;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.MessageLoggingHandler;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.part.hand.BlackHoleMessageHandler;
import com.barchart.netty.part.pipe.PipeAny;

/** use for arbiter source end points */
@Component(name = PipeArbSource.NAME, immediate = true)
public class PipeArbSource extends PipeAny implements NameArb {

	public static final String NAME = "barchart.netty.pipe.arbiter.source";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void apply(final NettyDot dot, final Channel channel, final Mode mode) {

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast(LOGGER, new MessageLoggingHandler());

		/** place holder for message redirect */
		pipeline.addLast(ARBITER, new BlackHoleMessageHandler());

	}

}
