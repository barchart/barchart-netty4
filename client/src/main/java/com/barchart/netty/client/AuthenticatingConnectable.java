package com.barchart.netty.client;

import rx.Observable;

import com.barchart.account.api.Account;
import com.barchart.account.api.AuthResult;

public interface AuthenticatingConnectable<T extends AuthenticatingConnectable<T, A>, A extends Account>
		extends Connectable<T> {

	public enum AuthState {

		/**
		 * No authentication attempt has been made.
		 */
		NOT_AUTHENTICATED,

		/**
		 * Client is about to send authentication.
		 */
		AUTHENTICATING,

		/**
		 * Authenticated succeeded.
		 */
		AUTHENTICATED,

		/**
		 * Authentication failed.
		 */
		AUTHENTICATION_FAILED

	}

	public interface Builder<C extends AuthenticatingConnectable<C, B>, B extends Account>
			extends Connectable.Builder<C> {

		/**
		 * Authenticate the connection with the given credentials. Credentials
		 * providers are specific to each Connectable implementation.
		 */
		Builder<? extends C, B> authenticator(
				Authenticator<B> authenticator);

	}

	/**
	 * Handler for authenticating the connection. Authentication providers are
	 * specific to each Connectable implementation.
	 */
	public interface Authenticator<B extends Account> {

		/**
		 * Authenticate the connection and return a result
		 * 
		 * @return
		 */
		Observable<AuthResult<B>> authenticate(MessageStream stream);

	}

	/**
	 * A message stream for authentication communication. This is passed as a
	 * separate object since the default send/receive methods may be blocked by
	 * client policy until authentication succeeds.
	 */
	public interface MessageStream {

		/**
		 * Send a message to the remote peer.
		 */
		<U> Observable<U> send(final U message);

		/**
		 * Subscribe to messages from the remote peer.
		 */
		<U> Observable<U> receive(final Class<U> type);

	}

	/**
	 * Observe authentication state changes.
	 */
	Observable<AuthState> authStateChanges();

	/**
	 * Check the last authentication state.
	 */
	AuthState authState();

	/**
	 * The authenticated account.
	 * 
	 * @return The account, or null if authentication is not complete or failed)
	 */
	A account();

}
