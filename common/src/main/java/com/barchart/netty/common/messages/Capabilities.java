/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.common.messages;

import java.util.Set;

/**
 * A list of capabilities that the peer supports for this connection, include
 * protocol versions, authentication methods and encryption support.
 */
public interface Capabilities {

	/**
	 * Pre-shared key authentication capability token.
	 */
	public static final String AUTH_PSK = "auth.psk";

	/**
	 * Password authentication capability token.
	 */
	public static final String AUTH_PASSWORD = "auth.password";

	/**
	 * TLS encryption support capability token.
	 */
	public static final String ENC_TLS = "encryption.tls";

	/**
	 * Plaintext allowed capability token.
	 */
	public static final String ENC_NONE = "encryption.none";

	/**
	 * A list of capabilities that the remote peer supports.
	 */
	Set<String> capabilities();

	/**
	 * The default/highest protocol version this server supports.
	 */
	Version version();

	/**
	 * The lowest protocol version this server supports.
	 */
	Version minVersion();

}