package com.barchart.netty.client.pipeline;

import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundMessageHandler;
import io.netty.channel.ChannelPromise;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Blocks the outbound flow of messages until the
 * 
 * @author jeremy
 * 
 */
public class OutboundMessageBlockingHandler extends ChannelDuplexHandler
		implements ChannelOutboundMessageHandler<Object> {

	private final Map<ChannelPromise, LinkedList<Object>> flushQueue =
			new HashMap<ChannelPromise, LinkedList<Object>>();

	private volatile boolean blocked;

	public OutboundMessageBlockingHandler() {
		this(false);
	}

	public OutboundMessageBlockingHandler(final boolean initialBlocked_) {
		blocked = initialBlocked_;
	}

	public boolean blocking() {
		return blocked;
	}

	public void block() {
		blocked = true;
	}

	public void unblock(final ChannelHandlerContext ctx) {

		synchronized (flushQueue) {

			blocked = false;

			if (flushQueue.size() > 0) {

				final MessageBuf<Object> nextBuffer =
						ctx.nextOutboundMessageBuffer();

				for (final Map.Entry<ChannelPromise, LinkedList<Object>> entry : flushQueue
						.entrySet()) {

					nextBuffer.addAll(entry.getValue());
					ctx.flush(entry.getKey());

				}

				flushQueue.clear();

			}

		}

	}

	@Override
	public void inboundBufferUpdated(final ChannelHandlerContext ctx)
			throws Exception {

		ctx.fireInboundBufferUpdated();

	}

	@Override
	public void flush(final ChannelHandlerContext ctx,
			final ChannelPromise promise) {

		if (blocked) {
			flushBlocked(ctx, promise);
		} else {
			flushReal(ctx, promise);
		}

	}

	private void flushBlocked(final ChannelHandlerContext ctx,
			final ChannelPromise promise) {

		synchronized (flushQueue) {

			// Check block state again now that we're synced
			if (blocked) {

				final MessageBuf<Object> messages = ctx.outboundMessageBuffer();

				final LinkedList<Object> queue = new LinkedList<Object>();
				messages.drainTo(queue);

				flushQueue.put(promise, queue);

			} else {
				flushReal(ctx, promise);
			}

		}

	}

	private void flushReal(final ChannelHandlerContext ctx,
			final ChannelPromise promise) {

		final MessageBuf<Object> messages = ctx.outboundMessageBuffer();
		final MessageBuf<Object> nextBuffer = ctx.nextOutboundMessageBuffer();

		for (;;) {
			final Object msg = messages.poll();
			if (msg == null) {
				break;
			}
			nextBuffer.add(msg);
		}

		ctx.flush(promise);

	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx)
			throws Exception {

		if (blocked && flushQueue.size() > 0) {

			for (final ChannelPromise promise : flushQueue.keySet()) {
				promise.setFailure(new ChannelException("Channel disconnected"));
			}

			flushQueue.clear();

		}

	}

	@Override
	public MessageBuf<Object> newOutboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		return Unpooled.messageBuffer();
	}

}
