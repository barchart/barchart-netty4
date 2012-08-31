package com.barchart.netty.part.pipe;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.MessageLoggingHandler;
import io.netty.util.CharsetUtil;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.host.api.NettyPipe;
import com.barchart.netty.part.hand.DatagramPacketWriter;
import com.barchart.netty.part.hand.SequenceWriter;

/**  */
@Component(name = PipeSequenceWriter.NAME, immediate = true)
public class PipeSequenceWriter implements NettyPipe {

	public static final String NAME = "barchart.netty.pipe.sequence.writer";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void apply(final Channel channel) {

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("logger", new MessageLoggingHandler());

		pipeline.addLast("datagram-writer", new DatagramPacketWriter());

		pipeline.addLast("encode-string", new StringEncoder(CharsetUtil.UTF_8));

		pipeline.addLast("sequence-writer", new SequenceWriter());

	}

}
