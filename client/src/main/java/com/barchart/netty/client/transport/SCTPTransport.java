/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.client.transport;

import io.netty.channel.sctp.nio.NioSctpChannel;

import java.net.URI;

public class SCTPTransport extends SimpleTransport {

	protected SCTPTransport(final URI uri) {
		super(uri, NioSctpChannel.class);
	}

}
