package com.barchart.netty.test.record;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.MessageLoggingHandler;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.part.hand.DatagramPacketReader;
import com.barchart.netty.part.pipe.PipeAny;

/**  */
@Component(name = PipeWrapRecorder.NAME, immediate = true)
public class PipeWrapRecorder extends PipeAny {

	public static final String NAME = "barchart.netty.pipe.record.wrapper";

	@Override
	public String componentName() {
		return NAME;
	}

	@Override
	public void apply(final NettyDot dot, final Channel channel, final Mode mode) {

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("logger", new MessageLoggingHandler());

		pipeline.addLast("datagram-reader", new DatagramPacketReader());

		pipeline.addLast("packet-recorder", new HandPacketWrapWriter());

	}

}
