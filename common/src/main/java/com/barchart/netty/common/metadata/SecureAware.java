/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
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