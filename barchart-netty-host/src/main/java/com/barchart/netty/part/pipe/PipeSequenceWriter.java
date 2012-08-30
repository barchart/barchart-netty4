package com.barchart.netty.part.pipe;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.host.api.NettyPipe;
import com.barchart.netty.part.hand.SequenceWriter;

/**  */
@Component(name = PipeSequenceWriter.NAME, immediate = true)
public class PipeSequenceWriter implements NettyPipe {

	public static final String NAME = "barchart.netty.pipe.sequence.wirter";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void apply(final Channel channel) {

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("encode-string", new StringEncoder(CharsetUtil.UTF_8));

		pipeline.addLast("sequence-writer", new SequenceWriter());

		pipeline.addLast("logger", new LoggingHandler());

	}

}
