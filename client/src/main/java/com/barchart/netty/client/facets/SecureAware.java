package com.barchart.netty.client.facets;

/**
 * A Connectable facet that indicates the secure status of the current
 * connection.
 */
public interface SecureAware {

	/**
	 * The connection security requested.
	 */
	enum Request {

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