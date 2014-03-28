/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.test.sequence;

import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;
import io.netty.channel.ChannelStateHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandSequenceReader extends ChannelStateHandlerAdapter implements
		ChannelInboundMessageHandler<Object> {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private ChannelHandlerContext ctx;

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {

		this.ctx = ctx;

	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx)
			throws Exception {

		this.ctx = null;

	}

	@Override
	public MessageBuf<Object> newInboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		return Unpooled.messageBuffer();
	}

	@Override
	public final void inboundBufferUpdated(final ChannelHandlerContext ctx)
			throws Exception {

		if (ctx.hasInboundMessageBuffer()) {
			processMessageBuffer(ctx);
		}

		ctx.fireInboundBufferUpdated();

	}

	private void processMessageBuffer(final ChannelHandlerContext ctx) {

		final MessageBuf<Object> source = ctx.inboundMessageBuffer();

		while (true) {

			final Object message = source.poll();

			if (message == null) {
				break;
			}

			if (message instanceof String) {
				final String packet = (String) message;
				readSequence(packet);
			}

		}

	}

	static final String PREFIX = "sequence=";

	private void readSequence(final String message) {

		if (message.startsWith(PREFIX)) {

			log.info("reader message : {}", message);

			final String text = message.replaceAll(PREFIX, "");

			final long sequence = Long.parseLong(text);

		}

	}

}
