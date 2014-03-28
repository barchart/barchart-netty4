/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.common.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.util.flow.api.Context;
import com.barchart.util.flow.api.Event;
import com.barchart.util.flow.api.Listener;
import com.barchart.util.flow.api.Point;
import com.barchart.util.flow.api.State;

/**
 * A Flow state machine that can function as a message "dam" while a Flow
 * machine is running on this channel. This allows you to perform complex
 * interactions with the remote host on connect (negotiating encryption,
 * websocket handshaking, authentication, etc) while blocking downstream
 * handlers from sending and receiving messages that may interrupt the process.
 *
 * If this handler is blocking, once the state machine is finished, the handler
 * unblocks the channel and removes itself from the pipeline to allow messages
 * to flow freely between the remote host and downstream handlers. Any messages
 * sent by downstream handlers while the Flow machine is running are queued and
 * flushed once all state machines are complete.
 *
 * Simple implementation example:
 *
 * <pre>
 * public class TestFlow extends MessageFlowHandler<MyEvent, MyState>() {
 *
 *    private final Flow<MyEvent, MyState, ChannelHandlerContext> flow;
 *
 *    public TestFlow() {
 *
 *        Flow.Builder<MyEvent, MyState, ChannelHandlerContext> builder =
 *            Provider.flowBuilder(MyEvent.class, MyState.class);
 *
 *        builder.listener(new OnError());
 *        builder.at(MyState.COMPLETE).listener(new OnComplete());
 *        builder.at(MyState.FAILED).listener(new OnFailed());
 *
 *        builder.at(MyState.START).on(MyEvent.PASS).to(MyState.COMPLETE);
 *        builder.at(MyState.START).on(MyEvent.FAIL).to(MyState.FAILED);
 *
 *        flow = builder.build();
 *
 *    }
 *
 *    @Override
 *    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
 *        context = flow.contextBuilder().build(ctx);
 *        super.channelActive(ctx);
 *    }
 *
 *    @Override
 *    protected boolean messageReceived(final Object message) throws IllegalStateException {
 *        // These are arbitrary message types that your pipeline has already decoded
 *        if (message instanceof ActionSucceeded) {
 *            context.fire(MyEvent.PASS);
 *        } else if (message instanceof ActionFailed) {
 *            context.fire(MyEvent.FAIL);
 *        }
 *    }
 *
 * }
 * </pre>
 *
 * @see com.barchart.util.flow.Flow
 */
public abstract class MessageFlowHandler<E extends Enum<E> & Event<E>, S extends Enum<S> & State<S>>
		extends ChannelInboundHandlerAdapter {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Queue<Object> inboundQueue = new LinkedList<Object>();

	private boolean blockActivate;

	/**
	 * Construct a new Flow handler that blocks downstream channel activation
	 * until it is complete.
	 */
	public MessageFlowHandler() {
		this(true);
	}

	/**
	 * Construct a new Flow handler.
	 *
	 * @param block_ True to block downstream channel activation until the state
	 *            machine completes.
	 */
	public MessageFlowHandler(final boolean blockActivate_) {
		blockActivate = blockActivate_;
	}

	/**
	 * Handle an inbound message, firing any events necessary on the current
	 * context. If a received message is not allowed given the current state,
	 * this method should throw an IllegalStateException.
	 *
	 * @param message The message
	 * @return True if the message was consumed, false to pass downstream
	 * @throw IllegalStateException If the message is not allowed given the
	 *        current state
	 */
	protected abstract boolean messageReceived(Object message)
			throws IllegalStateException;

	/**
	 * Must be called by subclass when the flow completes. The simplest way to
	 * do this is usually by using the OnComplete() state listener in your Flow.
	 *
	 * @see OnComplete
	 */
	protected void complete(final ChannelHandlerContext ctx) throws Exception {

		log.debug("Flow complete, flushing inbound message queue");

		// Notify downstream that connection is active
		if (blockActivate) {
			super.channelActive(ctx);
		}

		Object msg;
		for (;;) {
			msg = inboundQueue.poll();
			if (msg == null) {
				break;
			}
			ctx.fireChannelRead(msg);
		}

		ctx.fireChannelReadComplete();

		ctx.pipeline().remove(this);

	}

	private void error(final ChannelHandlerContext ctx, final Throwable t) {

		log.error("Exception in flow handler", t);
		ctx.fireExceptionCaught(t);

		fail(ctx);

	}

	/**
	 * Must be called by subclass when the flow fails. This re-fires the
	 * exception on the ChannelHandlerContext and closes the channel. The
	 * simplest way to do this is usually by using the OnError() and OnFailed()
	 * state listeners in your Flow.
	 *
	 * @see OnError
	 * @see OnFailed
	 */
	protected void fail(final ChannelHandlerContext ctx) {

		ctx.close();

	}

	/**
	 * Add a message to the inbound queue to be passed to downstream handlers
	 * when the state machine completes.
	 */
	protected void forwardOnComplete(final Object message) {
		log.trace("Forwarding on completion: " + message);
		inboundQueue.add(message);
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
		log.debug("Flow handler activated");
		if (!blockActivate) {
			super.channelActive(ctx);
		}
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object msg)
			throws Exception {

		log.trace("Message received: " + msg);

		try {
			if (!messageReceived(msg)) {
				ctx.fireChannelRead(msg);
			}
		} catch (final Throwable t) {
			error(ctx, t);
		}

	}

	/**
	 * Helper class for less parameter typing in subclasses.
	 */
	protected class StateTransition extends
			Listener.Adapter<E, S, ChannelHandlerContext> {

		public StateTransition() {
		}

	}

	/**
	 * A state transition listener for uniform handling of transition errors.
	 * When an error is raised, it calls MessageFlowHandler.error().
	 *
	 * It is recommended that all Flow instances use this as a global state
	 * listener:
	 *
	 * builder.listener(new OnError());
	 */
	protected class OnError extends StateTransition {

		public OnError() {
		}

		@Override
		public void enterError(final Point<E, S> past, final Point<E, S> next,
				final Context<E, S, ChannelHandlerContext> context,
				final Throwable cause) {
			error(context.attachment(), cause);
		}

		@Override
		public void leaveError(final Point<E, S> past, final Point<E, S> next,
				final Context<E, S, ChannelHandlerContext> context,
				final Throwable cause) {
			error(context.attachment(), cause);
		}

	}

	/**
	 * A state transition listener that marks this handler as failed and calls
	 * MessageFlowHandler.error().
	 *
	 * It is recommended that all Flow instances use this as a failed state
	 * listener:
	 *
	 * builder.at(FAILED_STATE).listener(new OnFailed());
	 */
	protected class OnFailed extends StateTransition {

		public OnFailed() {
		}

		@Override
		public void enter(final Point<E, S> past, final Point<E, S> next,
				final Context<E, S, ChannelHandlerContext> context)
				throws Exception {

			fail(context.attachment());

		}

	}

	/**
	 * A state transition listener that marks this handler as complete and calls
	 * MessageFlowHandler.complete().
	 *
	 * It is recommended that all Flow instances use this as a failed state
	 * listener:
	 *
	 * builder.at(COMPLETE_STATE).listener(new OnComplete());
	 */
	protected class OnComplete extends StateTransition {

		public OnComplete() {
		}

		@Override
		public void enter(final Point<E, S> past, final Point<E, S> next,
				final Context<E, S, ChannelHandlerContext> context)
				throws Exception {

			complete(context.attachment());

		}

	}

}
