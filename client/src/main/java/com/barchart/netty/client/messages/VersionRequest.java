package com.barchart.netty.client.messages;

/**
 * Request a specific protocol version from the peer.
 */
public interface VersionRequest {

	/**
	 * The protocol version requested.
	 */
	Version version();

}
