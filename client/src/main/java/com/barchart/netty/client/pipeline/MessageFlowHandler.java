package com.barchart.netty.client.pipeline;

import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;

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
		extends OutboundMessageBlockingHandler implements
		ChannelInboundMessageHandler<Object> {

	private final static Logger log = LoggerFactory
			.getLogger(MessageFlowHandler.class);

	private final Queue<Object> inboundQueue = new LinkedList<Object>();

	/**
	 * Construct a new Flow handler that blocks downstream communication until
	 * it is complete.
	 */
	public MessageFlowHandler() {
		this(true);
	}

	/**
	 * Construct a new Flow handler.
	 * 
	 * @param block_ True to block communication between the remote host and
	 *            downstream handlers until the state machine is complete
	 */
	public MessageFlowHandler(final boolean block_) {
		if (block_) {
			block();
		}
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
	protected void complete(final ChannelHandlerContext ctx) {

		log.debug("Completed flow");

		unblock(ctx);

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
	protected void error(final ChannelHandlerContext ctx, final Throwable t) {

		log.error("Exception in flow handler", t);

		ctx.fireExceptionCaught(t);
		ctx.close();

	}

	/**
	 * Add a message to the inbound queue to be passed to downstream handlers
	 * when the state machine completes.
	 */
	protected void forwardOnComplete(final Object message) {
		inboundQueue.add(message);
	}

	@Override
	public void unblock(final ChannelHandlerContext ctx) {

		log.debug("Flow complete, flushing message queues");

		// Forward queued messages to next handler
		final MessageBuf<Object> next = ctx.nextInboundMessageBuffer();

		next.addAll(inboundQueue);
		inboundQueue.clear();

		ctx.fireInboundBufferUpdated();

		super.unblock(ctx);

		ctx.pipeline().remove(this);

	}

	@Override
	public void inboundBufferUpdated(final ChannelHandlerContext ctx)
			throws Exception {

		final MessageBuf<Object> in = ctx.inboundMessageBuffer();
		final MessageBuf<Object> out = ctx.nextInboundMessageBuffer();

		for (;;) {

			final Object msg = in.poll();
			if (msg == null) {
				break;
			}

			try {
				if (!messageReceived(msg) && out != null) {
					out.add(msg);
				}
			} catch (final Throwable t) {
				error(ctx, t);
			}

		}

		ctx.fireInboundBufferUpdated();

	}

	@Override
	public MessageBuf<Object> newInboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		return Unpooled.messageBuffer();
	}

	/**
	 * Helper class for less parameter typing in subclasses.
	 */
	protected class StateTransition extends
			Listener.Adapter<E, S, ChannelHandlerContext> {
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

		@Override
		public void enter(final Point<E, S> past, final Point<E, S> next,
				final Context<E, S, ChannelHandlerContext> context)
				throws Exception {

			error(context.attachment(),
					new Exception(
							"Flow machine failed at the following transition: "
									+ past.state() + " -> " + next.event()
									+ " -> " + next.state()));

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

		@Override
		public void enter(final Point<E, S> past, final Point<E, S> next,
				final Context<E, S, ChannelHandlerContext> context)
				throws Exception {

			complete(context.attachment());

		}

	}

}
