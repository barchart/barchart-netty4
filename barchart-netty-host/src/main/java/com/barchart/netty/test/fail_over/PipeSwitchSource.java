package com.barchart.netty.test.fail_over;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.MessageLoggingHandler;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.part.hand.BlackHoleMessageHandler;
import com.barchart.netty.part.pipe.PipeAny;

/** use for switch source end points */
@Component(name = PipeSwitchSource.TYPE, immediate = true)
public class PipeSwitchSource extends PipeAny implements NameSwitch {

	public static final String TYPE = "barchart.netty.pipe.switch.source";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public void apply(final NettyDot dot, final Channel channel, final Mode mode) {

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast(LOGGER, new MessageLoggingHandler());

		/** place holder for message redirect */
		pipeline.addLast(SWITCH, new BlackHoleMessageHandler());

	}

}
