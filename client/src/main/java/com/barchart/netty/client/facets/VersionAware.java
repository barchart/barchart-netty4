package com.barchart.netty.client.facets;

import com.barchart.netty.client.messages.Version;

/**
 * A Connectable facet that returns the agreed-upon protocol version for this
 * connection.
 */
public interface VersionAware {

	/**
	 * The protocol version this connection is using.
	 */
	Version version();

}