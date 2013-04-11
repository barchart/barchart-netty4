package com.barchart.netty.test.sequence;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.MessageLoggingHandler;
import io.netty.util.CharsetUtil;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.part.hand.DatagramPacketReader;
import com.barchart.netty.pipe.PipeAny;

/**  */
@Component(name = PipeSequenceReader.TYPE, immediate = true)
public class PipeSequenceReader extends PipeAny {

	public static final String TYPE = "barchart.netty.pipe.sequence.reader";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public void apply(final Channel channel, final Mode mode) {

		log.debug("apply : {}", channel);

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("logger", new MessageLoggingHandler());

		pipeline.addLast("packet-reader", new DatagramPacketReader());

		pipeline.addLast("string-decoder", new StringDecoder(CharsetUtil.UTF_8));

		pipeline.addLast("sequence-reader", new HandSequenceReader());

	}

}
