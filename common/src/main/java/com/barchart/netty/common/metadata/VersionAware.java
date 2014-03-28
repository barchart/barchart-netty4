/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
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