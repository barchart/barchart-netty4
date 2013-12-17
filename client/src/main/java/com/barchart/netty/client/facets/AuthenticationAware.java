package com.barchart.netty.client.facets;

import rx.Observable;

import com.barchart.account.api.Account;
import com.barchart.account.api.AuthResult;

public interface AuthenticationAware<A extends Account> {

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
