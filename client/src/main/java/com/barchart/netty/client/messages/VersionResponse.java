package com.barchart.netty.client.messages;

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
