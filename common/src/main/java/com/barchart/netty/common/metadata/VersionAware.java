package com.barchart.netty.common.metadata;

import com.barchart.netty.common.messages.Version;

/**
 * Protocol version monitoring for a Connectable.
 */
public interface VersionAware {

	/**
	 * The protocol version this connection is using.
	 */
	Version version();

}