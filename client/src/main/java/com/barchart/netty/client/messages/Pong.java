package com.barchart.netty.client.messages;

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
