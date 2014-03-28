/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.common.metadata;

/**
 * Latency and clock skew monitoring for a Connectable.
 */
public interface LatencyAware {

	/**
	 * The latest estimate of network latency between us and the remote peer.
	 */
	double averageLatency();

	/**
	 * The latest estimate of network latency between us and the remote peer.
	 */
	long latency();

	/**
	 * The estimated clock difference between the client and remote peer.
	 * 
	 * Skew may be significant for some clients, so any time-synchronized
	 * calculations (i.e. MFA or message signatures) should take this into
	 * account.
	 * 
	 * @see LatencyAware#peerTime()
	 */
	long clockSkew();

	/**
	 * The estimated timestamp of the remote peer.
	 * 
	 * This is essentially System.currentTimeMillis() + clockSkew().
	 */
	long peerTime();

}
