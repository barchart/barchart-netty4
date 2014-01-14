package com.barchart.netty.common.metadata;

/**
 * Secure channel status monitoring for a Connectable.
 */
public interface SecureAware {

	/**
	 * The connection security requested.
	 */
	enum Encryption {

		/**
		 * Require a secure connection.
		 */
		REQUIRE,

		/**
		 * Activate a secure connection if the remote host supports it.
		 */
		OPTIONAL,

		/**
		 * Do not use a secure connection.
		 */
		REFUSE

	};

	/**
	 * Check if this connection is secure.
	 * 
	 * @return true if encryption is activated.
	 */
	boolean secure();

}