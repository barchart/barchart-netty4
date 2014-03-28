/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.common.messages;

/**
 * Request a specific protocol version from the peer.
 */
public interface VersionRequest {

	/**
	 * The protocol version requested.
	 */
	Version version();

}
