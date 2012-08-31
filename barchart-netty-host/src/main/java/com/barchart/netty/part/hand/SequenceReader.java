package com.barchart.netty.part.hand;

import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceReader extends ChannelHandlerAdapter implements
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

	private void readSequence(final String packet) {

		if (packet.startsWith(PREFIX)) {

			final String text = packet.replaceAll(PREFIX, "");

			final long sequence = Long.parseLong(text);

			log.info("sequence : {}", sequence);

		}

	}

}