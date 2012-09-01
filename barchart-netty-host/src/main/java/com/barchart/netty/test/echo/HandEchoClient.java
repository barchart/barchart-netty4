package com.barchart.netty.test.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundByteHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class HandEchoClient extends ChannelInboundByteHandlerAdapter {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private ByteBuf message;

	/**
	 */
	public HandEchoClient(final int messageSize) {

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

	@Override
	public void inboundBufferUpdated(final ChannelHandlerContext ctx,
			final ByteBuf source) {

		final ByteBuf target = ctx.nextOutboundByteBuffer();

		target.discardReadBytes();

		target.writeBytes(source);

		ctx.flush();

		printStatus();

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

}
