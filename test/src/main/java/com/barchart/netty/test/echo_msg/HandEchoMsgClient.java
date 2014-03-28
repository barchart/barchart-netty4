/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.test.echo_msg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class HandEchoMsgClient extends
		ChannelInboundMessageHandlerAdapter<Object> {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private ByteBuf message;

	/**
	 */
	public HandEchoMsgClient(final int messageSize) {

		makeMessage(messageSize);

	}

	private void makeMessage(final int size) {

		message = Unpooled.buffer(size);

		for (int k = 0; k < message.capacity(); k++) {
			message.writeByte((byte) k);
		}

	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) {

		ctx.write(message);

	}

	private long count;

	private void printStatus() {

		if (count % 10000 == 0) {
			log.debug("client ping count = {}", count);
		}

		count++;

	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx,
			final Throwable cause) {

		log.error("unexpected", cause);

		ctx.close();

	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
			final Object msg) throws Exception {

		final MessageBuf<Object> target = ctx.nextOutboundMessageBuffer();

		target.add(msg);

		ctx.flush();

		printStatus();

	}

}
