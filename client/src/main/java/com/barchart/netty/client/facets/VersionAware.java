package com.barchart.netty.client.facets;

import com.barchart.netty.client.messages.Version;

/**
 * Protocol version monitoring for a Connectable.
 */
public interface VersionAware {

	/**
	 * The protocol version this connection is using.
	 */
	Version version();

}