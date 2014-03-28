/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.common.messages;

/**
 * A heartbeat ping to the remote peer. When the peer receives this message,
 * they are expected to immediately send a Pong response.
 */
public interface Ping {

	/**
	 * The local timestamp at the time this ping was sent.
	 */
	long timestamp();

}
