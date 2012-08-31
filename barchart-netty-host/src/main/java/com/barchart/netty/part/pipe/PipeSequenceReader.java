package com.barchart.netty.part.pipe;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.MessageLoggingHandler;
import io.netty.util.CharsetUtil;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.host.api.NettyPipe;
import com.barchart.netty.part.hand.DatagramPacketReader;
import com.barchart.netty.part.hand.SequenceReader;

/**  */
@Component(name = PipeSequenceReader.NAME, immediate = true)
public class PipeSequenceReader implements NettyPipe {

	public static final String NAME = "barchart.netty.pipe.sequence.reader";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void apply(final Channel channel) {

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("logger", new MessageLoggingHandler());

		pipeline.addLast("datagram-reader", new DatagramPacketReader());

		pipeline.addLast("decode-string", new StringDecoder(CharsetUtil.UTF_8));

		pipeline.addLast("sequence-reader", new SequenceReader());

	}

}
