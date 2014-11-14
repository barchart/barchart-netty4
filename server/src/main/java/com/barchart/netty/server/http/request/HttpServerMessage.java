/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.request;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpVersion;

/**
 * Information about an inbound request.
 */
public interface HttpServerMessage extends HttpMessage {

	@Override
	public HttpHeaders headers();

	@Override
	public HttpVersion getProtocolVersion();

}
