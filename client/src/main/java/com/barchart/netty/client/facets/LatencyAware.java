package com.barchart.netty.client.facets;

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
