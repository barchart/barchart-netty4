/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.test.sequence;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.MessageLoggingHandler;
import io.netty.util.CharsetUtil;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.part.hand.DatagramPacketWriter;
import com.barchart.netty.pipe.PipeAny;

/**  */
@Component(name = PipeSequenceWriter.TYPE, immediate = true)
public class PipeSequenceWriter extends PipeAny {

	public static final String TYPE = "barchart.netty.pipe.sequence.writer";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public void apply(final Channel channel, final Mode mode) {

		log.debug("apply : {}", channel);

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("logger", new MessageLoggingHandler());

		pipeline.addLast("packet-writer", new DatagramPacketWriter());

		pipeline.addLast("string-encoder", new StringEncoder(CharsetUtil.UTF_8));

		pipeline.addLast("sequence-writer", new HandSequenceWriter());

	}

}
