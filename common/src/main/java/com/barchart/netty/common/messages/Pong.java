/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.common.messages;

/**
 * A response to a heartbeat Ping message from a peer. This should be sent
 * immediately on receipt of a Ping.
 */
public interface Pong {

	/**
	 * The local timestamp at the time of sending.
	 */
	long timestamp();

	/**
	 * The peer timestamp as sent in the original Ping message.
	 */
	long pinged();

}
