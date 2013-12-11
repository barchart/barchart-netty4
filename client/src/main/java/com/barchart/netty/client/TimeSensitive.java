package com.barchart.netty.client;

public interface TimeSensitive {

	/**
	 * The latest estimate of network latency between us and the remote peer.
	 */
	double averageLatency();

	/**
	 * The latest estimate of network latency between us and the remote peer.
	 */
	long latency();

	/**
	 * The estimated clock difference between the client and remote peer. This
	 * may be significant for some clients. Any time-critical calculations (i.e.
	 * MFA or message signatures) must take this skew into account
	 */
	long clockSkew();

}
