package com.barchart.netty.client.protobuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SslHandler;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;

import com.barchart.account.api.Account;
import com.barchart.account.api.Account.Scheme;
import com.barchart.account.api.AuthResult;
import com.barchart.account.api.AuthResult.Status;
import com.barchart.account.common.request.AuthRequest;
import com.barchart.netty.client.facets.AuthenticationAware;
import com.barchart.netty.client.messages.Capabilities;
import com.barchart.netty.client.pipeline.AuthenticationHandler;
import com.barchart.netty.client.pipeline.MessageFlowHandler;
import com.barchart.netty.client.protobuf.PasswordAuthFlowHandler.AuthEvent;
import com.barchart.netty.client.protobuf.PasswordAuthFlowHandler.AuthState;
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
public class PasswordAuthFlowHandler extends
		MessageFlowHandler<AuthEvent, AuthState> implements
		AuthenticationHandler<Account> {

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

	// Context
	private Context<AuthEvent, AuthState, ChannelHandlerContext> context = null;
	private Capabilities capabilities = null;
	private Account account = null;

	private final Flow<AuthEvent, AuthState, ChannelHandlerContext> flow;
	private final String username;
	private final char[] password;
	private final String deviceId;
	private final String source;

	public PasswordAuthFlowHandler(final String username_,
			final char[] password_, final String deviceId_, final String source_) {

		username = username_;
		password = password_;
		deviceId = deviceId_;
		source = source_;

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
		} else if (message instanceof AuthResult) {
			final AuthResult<?> result = (AuthResult<?>) message;
			if (result.status() == Status.AUTHENTICATED) {
				account = result.account();
				context.fire(AuthEvent.AUTH_OK);
			} else {
				context.fire(AuthEvent.AUTH_FAILED);
			}
			return true;
		}

		// No extra messages allowed until negotiation finishes
		throw new IllegalStateException("Unexpected message type: "
				+ message.getClass());

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

			if (ctx.pipeline().get(SslHandler.class) == null) {

				if (!capabilities.capabilities().contains(
						Capabilities.AUTH_PASSWORD)) {
					throw new UnsupportedOperationException(
							"Password authentication not supported by peer");
				}

				// Defaults
				final AuthRequest req = new AuthRequest();
				req.scheme(Scheme.CLIENT.name());
				req.username(username);
				req.secret(password);
				req.domain("barchart.com");
				req.deviceId(deviceId);
				req.source(source);

				try {

					// Parse use new-style account URIs
					final URI acctUri =
							new URI(URLDecoder.decode(username, "UTF-8"));

					if (acctUri.getScheme() != null) {
						req.scheme(acctUri.getScheme().toUpperCase());
						req.username(acctUri.getPath().substring(1));
						req.domain(acctUri.getHost());
					}

				} catch (final URISyntaxException ufe) {
					// Not a URI, treat as simple username for legacy
					// compatibility
				}

				authStateChanges
						.onNext(AuthenticationAware.AuthState.AUTHENTICATING);

				ctx.write(req);
				ctx.flush();

				// Fire event to advance state machine
				context.fire(AuthEvent.AUTH_REQUEST);

			} else {

				context.fire(AuthEvent.PASS);

			}

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
	public Account account() {
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

	public static class Builder implements
			AuthenticationHandler.Builder<Account> {

		private final String username;
		private final char[] password;
		private final String deviceId;
		private final String source;

		public Builder(final String username_, final char[] password_,
				final String deviceId_, final String source_) {

			username = username_;
			password = password_;
			deviceId = deviceId_;
			source = source_;

		}

		@Override
		public AuthenticationHandler<Account> build() {
			return new PasswordAuthFlowHandler(username, password, deviceId,
					source);
		}

	}

}
