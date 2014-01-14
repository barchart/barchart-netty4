package com.barchart.netty.client.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import com.barchart.netty.client.pipeline.SecureFlowHandler.TLSEvent;
import com.barchart.netty.client.pipeline.SecureFlowHandler.TLSState;
import com.barchart.netty.common.messages.Capabilities;
import com.barchart.netty.common.messages.StartTLS;
import com.barchart.netty.common.messages.StopTLS;
import com.barchart.netty.common.metadata.SecureAware;
import com.barchart.netty.common.pipeline.MessageFlowHandler;
import com.barchart.util.flow.api.Context;
import com.barchart.util.flow.api.Event;
import com.barchart.util.flow.api.Flow;
import com.barchart.util.flow.api.Point;
import com.barchart.util.flow.api.State;
import com.barchart.util.flow.provider.Provider;

/**
 * Flow state machine for activating TLS on connect. Not sharable between
 * multiple connections.
 */
public class SecureFlowHandler extends MessageFlowHandler<TLSEvent, TLSState>
		implements SecureAware {

	public static enum TLSEvent implements Event<TLSEvent> {
		CONNECTED, //
		CAPABILITIES, //
		START_TLS, //
		TLS_OK, //
		TLS_STARTED, //
		TLS_FAILED, //
		PASS
	}

	public static enum TLSState implements State<TLSState> {
		CONNECTED, //
		NEGOTIATING, //
		STARTING_TLS, //
		ACTIVATING_TLS, //
		COMPLETE, //
		FAILED
	}

	private final boolean require;
	private final Flow<TLSEvent, TLSState, ChannelHandlerContext> flow;

	private boolean secure = false;

	private Context<TLSEvent, TLSState, ChannelHandlerContext> context = null;
	private Capabilities capabilities = null;

	public SecureFlowHandler(final boolean require_) {
		require = require_;
		flow = buildFlow();
	}

	private Flow<TLSEvent, TLSState, ChannelHandlerContext> buildFlow() {

		final Flow.Builder<TLSEvent, TLSState, ChannelHandlerContext> fb =
				Provider.flowBuilder(TLSEvent.class, TLSState.class);

		/*
		 * Initial states
		 */

		fb.initial(TLSState.CONNECTED);
		fb.initial(TLSEvent.CONNECTED);
		fb.terminal(TLSState.COMPLETE);
		fb.terminal(TLSEvent.PASS);

		/*
		 * State listeners
		 */

		fb.listener(new OnError());
		fb.at(TLSState.NEGOTIATING).listener(new Negotiating());
		fb.at(TLSState.ACTIVATING_TLS).listener(new ActivatingTLS());
		fb.at(TLSState.FAILED).listener(new OnFailed());
		fb.at(TLSState.COMPLETE).listener(new OnComplete());

		/*
		 * State transitions
		 */

		// Received capabilities
		fb.at(TLSState.CONNECTED).on(TLSEvent.CAPABILITIES)
				.to(TLSState.NEGOTIATING);

		// TLS
		fb.at(TLSState.NEGOTIATING).on(TLSEvent.START_TLS)
				.to(TLSState.STARTING_TLS);
		fb.at(TLSState.STARTING_TLS).on(TLSEvent.TLS_OK)
				.to(TLSState.ACTIVATING_TLS);
		fb.at(TLSState.STARTING_TLS).on(TLSEvent.TLS_FAILED)
				.to(TLSState.FAILED);
		fb.at(TLSState.ACTIVATING_TLS).on(TLSEvent.TLS_STARTED)
				.to(TLSState.COMPLETE);
		fb.at(TLSState.ACTIVATING_TLS).on(TLSEvent.TLS_FAILED)
				.to(TLSState.FAILED);

		fb.enforce(true);

		return fb.build();

	}

	@Override
	public boolean secure() {
		return secure;
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
		context = flow.contextBuilder().build(ctx);
		super.channelActive(ctx);
	}

	@Override
	protected boolean messageReceived(final Object message)
			throws IllegalStateException {

		if (message instanceof Capabilities) {
			capabilities = (Capabilities) message;
			forwardOnComplete(capabilities);
			context.fire(TLSEvent.CAPABILITIES);
			return true;
		} else if (message instanceof StartTLS) {
			context.fire(TLSEvent.TLS_OK);
			return true;
		} else if (message instanceof StopTLS) {
			context.fire(TLSEvent.TLS_FAILED);
			return true;
		}

		// No extra messages allowed until negotiation finishes
		throw new IllegalStateException("Unexpected message type: "
				+ message.getClass());

	}

	private class Negotiating extends StateTransition {

		@Override
		public void enter(final Point<TLSEvent, TLSState> past,
				final Point<TLSEvent, TLSState> next,
				final Context<TLSEvent, TLSState, ChannelHandlerContext> context)
				throws Exception {

			if (capabilities == null) {
				throw new IllegalStateException("Capabilities not received yet");
			}

			final ChannelHandlerContext ctx = context.attachment();

			if (ctx.pipeline().get(SslHandler.class) == null) {

				if (require
						&& !capabilities.capabilities().contains(
								Capabilities.ENC_TLS)) {
					throw new UnsupportedOperationException(
							"TLS required but not supported by peer");
				}

				// Start TLS handshake
				ctx.write(new StartTLS() {
				});
				ctx.flush();

				// Fire event to advance state machine
				context.fire(TLSEvent.START_TLS);

			} else {

				context.fire(TLSEvent.PASS);

			}

		}

	}

	private class ActivatingTLS extends StateTransition {

		@Override
		public void enter(final Point<TLSEvent, TLSState> past,
				final Point<TLSEvent, TLSState> next,
				final Context<TLSEvent, TLSState, ChannelHandlerContext> context)
				throws Exception {

			final ChannelHandlerContext ctx = context.attachment();

			final SSLEngine sslEngine =
					SSLContext.getDefault().createSSLEngine();
			sslEngine.setUseClientMode(true);

			final SslHandler handler = new SslHandler(sslEngine, false);

			handler.handshakeFuture().addListener(
					new GenericFutureListener<Future<Channel>>() {

						@Override
						public void operationComplete(
								final Future<Channel> future) throws Exception {

							if (future.isSuccess()) {

								secure = true;

								context.fire(TLSEvent.TLS_STARTED);

							} else {

								secure = false;

								if (require) {

									context.fire(TLSEvent.TLS_FAILED);

								} else {

									// Not required, remove the handler and try
									// to carry on
									context.attachment().pipeline()
											.remove(SslHandler.class);
									context.fire(TLSEvent.PASS);

								}

							}

						}

					});

			// Add SslHandler to pipeline to initiate handshake
			ctx.pipeline().addFirst(handler);

		}
	}

}
