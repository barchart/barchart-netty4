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
