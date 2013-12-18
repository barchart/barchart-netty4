package com.barchart.netty.client.facets;

import rx.Observable;

/**
 * Authentication state management and monitoring for a Connectable.
 */
public interface AuthenticationAware<A> {

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
