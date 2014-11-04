package com.barchart.netty.client.pipeline;

import io.netty.channel.ChannelHandlerContext;
import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;

import com.barchart.netty.client.pipeline.AuthFlowHandler.AuthEvent;
import com.barchart.netty.client.pipeline.AuthFlowHandler.AuthState;
import com.barchart.netty.common.messages.Capabilities;
import com.barchart.netty.common.metadata.AuthenticationAware;
import com.barchart.netty.common.pipeline.MessageFlowHandler;
import com.barchart.util.flow.api.Context;
import com.barchart.util.flow.api.Event;
import com.barchart.util.flow.api.Flow;
import com.barchart.util.flow.api.Point;
import com.barchart.util.flow.api.State;
import com.barchart.util.flow.provider.Provider;

/**
 * Flow state machine for activating TLS on connect. Not sharable between multiple connections.
 */
public abstract class AuthFlowHandler<A> extends
		MessageFlowHandler<AuthEvent, AuthState> implements
		AuthenticationHandler<A> {

	public static enum AuthEvent implements Event<AuthEvent> {
		CONNECTED, //
		CAPABILITIES, //
		AUTH_REQUEST, //
		AUTH_OK, //
		AUTH_FAILED, //
		PASS
	}

	public static enum AuthState implements State<AuthState> {
		CONNECTED, //
		NEGOTIATING, //
		AUTHENTICATING, //
		COMPLETE, //
		FAILED
	}

	// Auth state
	private final PublishSubject<AuthenticationAware.AuthState> authStateChanges =
			PublishSubject.create();
	private AuthenticationAware.AuthState lastState =
			AuthenticationAware.AuthState.NOT_AUTHENTICATED;

	// State machine context
	private final Flow<AuthEvent, AuthState, ChannelHandlerContext> flow;
	private Context<AuthEvent, AuthState, ChannelHandlerContext> context = null;
	private A account = null;

	protected Capabilities capabilities = null;

	protected AuthFlowHandler() {

		flow = buildFlow();

		authStateChanges.subscribe(new AuthStateObserver());

	}

	private Flow<AuthEvent, AuthState, ChannelHandlerContext> buildFlow() {

		final Flow.Builder<AuthEvent, AuthState, ChannelHandlerContext> fb =
				Provider.flowBuilder(AuthEvent.class, AuthState.class);

		/*
		 * Initial states
		 */

		fb.initial(AuthState.CONNECTED);
		fb.initial(AuthEvent.CONNECTED);
		fb.terminal(AuthState.COMPLETE);
		fb.terminal(AuthEvent.PASS);

		/*
		 * State listeners
		 */

		fb.listener(new OnError());
		fb.at(AuthState.NEGOTIATING).listener(new Negotiating());
		fb.at(AuthState.FAILED).listener(new OnAuthFailed());
		fb.at(AuthState.COMPLETE).listener(new OnAuthComplete());

		/*
		 * State transitions
		 */

		// Received capabilities
		fb.at(AuthState.CONNECTED).on(AuthEvent.CAPABILITIES)
				.to(AuthState.NEGOTIATING);

		// Authentication
		fb.at(AuthState.NEGOTIATING).on(AuthEvent.AUTH_REQUEST)
				.to(AuthState.AUTHENTICATING);
		fb.at(AuthState.AUTHENTICATING).on(AuthEvent.AUTH_OK)
				.to(AuthState.COMPLETE);
		fb.at(AuthState.AUTHENTICATING).on(AuthEvent.AUTH_FAILED)
				.to(AuthState.FAILED);

		fb.enforce(true);

		return fb.build();

	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
		context = flow.contextBuilder().build(ctx);
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx)
			throws Exception {
		context = null;
		lastState = AuthenticationAware.AuthState.NOT_AUTHENTICATED;
		super.channelInactive(ctx);
	}

	@Override
	protected boolean messageReceived(final Object message)
			throws IllegalStateException {

		if (message instanceof Capabilities) {

			capabilities = (Capabilities) message;
			forwardOnComplete(capabilities);

			context.fire(AuthEvent.CAPABILITIES);

			return true;

		} else {

			final A acct = response(message);

			if (acct != null) {
				account = acct;
				context.fire(AuthEvent.AUTH_OK);
			} else {
				context.fire(AuthEvent.AUTH_FAILED);
			}

			return true;

		}

	}

	private class Negotiating extends StateTransition {

		@Override
		public void enter(
				final Point<AuthEvent, AuthState> past,
				final Point<AuthEvent, AuthState> next,
				final Context<AuthEvent, AuthState, ChannelHandlerContext> context)
				throws Exception {

			if (capabilities == null) {
				throw new IllegalStateException("Capabilities not received yet");
			}

			final ChannelHandlerContext ctx = context.attachment();

			if (!capabilities.capabilities().contains(
					Capabilities.AUTH_PASSWORD)) {
				throw new UnsupportedOperationException(
						"Password authentication not supported by peer");
			}

			authStateChanges.onNext(AuthenticationAware.AuthState.AUTHENTICATING);

			authenticate(ctx);

			// Fire event to advance state machine
			context.fire(AuthEvent.AUTH_REQUEST);

		}

	}

	@Override
	public Observable<AuthenticationAware.AuthState> authStateChanges() {
		return authStateChanges;
	}

	@Override
	public AuthenticationAware.AuthState authState() {
		return lastState;
	}

	@Override
	public A account() {
		return account;
	}

	private class AuthStateObserver implements
			Observer<AuthenticationAware.AuthState> {

		@Override
		public void onNext(final AuthenticationAware.AuthState state) {
			lastState = state;
		}

		@Override
		public void onCompleted() {
		}

		@Override
		public void onError(final Throwable e) {
		}

	}

	private class OnAuthFailed extends OnFailed {

		@Override
		public void enter(
				final Point<AuthEvent, AuthState> past,
				final Point<AuthEvent, AuthState> next,
				final Context<AuthEvent, AuthState, ChannelHandlerContext> context)
				throws Exception {

			super.enter(past, next, context);

			authStateChanges
					.onNext(AuthenticationAware.AuthState.AUTHENTICATION_FAILED);

		}

	}

	private class OnAuthComplete extends OnComplete {

		@Override
		public void enter(
				final Point<AuthEvent, AuthState> past,
				final Point<AuthEvent, AuthState> next,
				final Context<AuthEvent, AuthState, ChannelHandlerContext> context)
				throws Exception {

			authStateChanges
					.onNext(AuthenticationAware.AuthState.AUTHENTICATED);

			super.enter(past, next, context);

		}

	}

}
