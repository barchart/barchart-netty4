/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.common.messages;

/**
 * Peer response to a Version request message.
 */
public interface VersionResponse {

	/**
	 * True if the protocol version requested is supported.
	 */
	boolean success();

	/**
	 * The current protocol version that the peer is expecting.
	 */
	Version version();

}
