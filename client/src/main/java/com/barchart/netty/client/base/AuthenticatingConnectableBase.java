package com.barchart.netty.client.base;

import io.netty.channel.EventLoopGroup;

import java.net.InetSocketAddress;

import rx.Observable;
import rx.Observer;

import com.barchart.account.api.Account;
import com.barchart.account.api.AuthResult;
import com.barchart.account.api.AuthResult.Status;
import com.barchart.netty.client.AuthenticatingConnectable;
import com.barchart.netty.client.Connectable;
import com.barchart.netty.client.transport.TransportProtocol;

public abstract class AuthenticatingConnectableBase<T extends AuthenticatingConnectableBase<T, A>, A extends Account>
		extends TimeSensitiveConnectableBase<T> implements
		AuthenticatingConnectable<T, A> {

	public abstract static class Builder<B extends Builder<B, C, D>, C extends AuthenticatingConnectableBase<C, D>, D extends Account>
			extends ConnectableBase.Builder<B, C> implements
			AuthenticatingConnectable.Builder<C, D> {

		protected Authenticator<D> authenticator;

		@SuppressWarnings("unchecked")
		@Override
		public B authenticator(final Authenticator<D> authenticator_) {
			authenticator = authenticator_;
			return (B) this;
		}

		@Override
		protected C configure(final C client) {
			super.configure(client);
			client.authenticator(authenticator);
			return client;
		}

	}

	/* Authentication state */
	private final AuthStateChange authStateChanges = new AuthStateChange();

	/* Authenticator */
	private Authenticator<A> authenticator = null;
	private A account = null;

	protected AuthenticatingConnectableBase(final EventLoopGroup eventLoop_,
			final InetSocketAddress address_, final TransportProtocol transport_) {

		super(eventLoop_, address_, transport_);

		stateChanges().subscribe(new ConnectionMonitor());

	}

	protected void authenticator(final Authenticator<A> authenticator_) {

		authenticator = authenticator_;

		if (state() == State.CONNECTED) {
			authStateChanges.fire(AuthState.AUTHENTICATING);
			authenticator.authenticate(new RawMessageStream()).subscribe(
					new AuthResultObserver());
		}

	}

	protected Authenticator<A> authenticator() {
		return authenticator;
	}

	@Override
	public Observable<AuthState> authStateChanges() {
		return Observable.create(authStateChanges);
	}

	@Override
	public AuthState authState() {
		return authStateChanges.last();
	}

	@Override
	public A account() {
		return account;
	}

	/**
	 * Send a message to the connected peer. The message type must be supported
	 * by the internal Netty pipeline.
	 * 
	 * @param message An object to encode and send to the remote peer
	 * @throws IllegalAccessError if an authenticator was provided but
	 *             authentication did not succeed
	 */
	@Override
	protected <U> Observable<U> send(final U message) {

		if (authenticator == null || authState() == AuthState.AUTHENTICATED) {
			return super.send(message);
		}

		throw new IllegalStateException("Authentication required");

	}

	/**
	 * Monitor connection state to handler authentication.
	 */
	private class ConnectionMonitor implements Observer<Connectable.State> {

		@Override
		public void onNext(final Connectable.State state) {

			switch (state) {

				case CONNECTED:

					if (authenticator != null) {
						authStateChanges.fire(AuthState.AUTHENTICATING);
						authenticator.authenticate(new RawMessageStream())
								.subscribe(new AuthResultObserver());
					}

					break;

				default:

					if (authStateChanges.last() != AuthState.NOT_AUTHENTICATED) {
						account = null;
						authStateChanges.fire(AuthState.NOT_AUTHENTICATED);
					}

			}

		}

		@Override
		public void onCompleted() {
		}

		@Override
		public void onError(final Throwable e) {
		}

	}

	/**
	 * Bypass the overridden methods in this class to provide access to the raw
	 * message stream for authentication.
	 */
	private class RawMessageStream implements MessageStream {

		@Override
		public <U> Observable<U> send(final U message) {
			return AuthenticatingConnectableBase.super.send(message);
		}

		@Override
		public <U> Observable<U> receive(final Class<U> type) {
			return AuthenticatingConnectableBase.super.receive(type);
		}
	}

	/**
	 * Monitor authentication results.
	 */
	private class AuthResultObserver implements Observer<AuthResult<A>> {

		@Override
		public void onNext(final AuthResult<A> result) {
			if (result.status() == Status.AUTHENTICATED) {
				account = result.account();
				authStateChanges.fire(AuthState.AUTHENTICATED);
			} else {
				account = null;
				authStateChanges.fire(AuthState.AUTHENTICATION_FAILED);
			}
		}

		@Override
		public void onCompleted() {
		}

		@Override
		public void onError(final Throwable e) {
		}

	}

	/**
	 * Notify of authentication state changes.
	 */
	private static class AuthStateChange extends PushSubscription<AuthState> {

		private AuthState lastState = AuthState.NOT_AUTHENTICATED;

		protected void fire(final AuthState state) {
			log.debug("Connection state fired: " + state);
			lastState = state;
			push(state);
		}

		public AuthState last() {
			return lastState;
		}

	}

}
