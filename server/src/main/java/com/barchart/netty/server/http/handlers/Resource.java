/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.handlers;

import java.io.IOException;
import java.io.InputStream;

public interface Resource {

	long modified();

	long size();

	String contentType();

	InputStream stream() throws IOException;

}