package com.barchart.netty.test.echo_msg;

import io.netty.buffer.MessageBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class HandEchoMsgServer extends
		ChannelInboundMessageHandlerAdapter<Object> {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx,
			final Throwable cause) {

		log.error("unexpected", cause);

		ctx.close();

	}

	private long count;

	private void printStatus() {

		if (count % 10000 == 0) {
			log.debug("server pong count = {}", count);
		}

		count++;

	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
			final Object msg) throws Exception {

		final MessageBuf<Object> out = ctx.nextOutboundMessageBuffer();

		out.add(msg);

		ctx.flush();

		printStatus();

	}

}
