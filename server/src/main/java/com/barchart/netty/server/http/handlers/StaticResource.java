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
import java.net.URL;

import javax.activation.MimetypesFileTypeMap;

public class StaticResource implements Resource {

	public static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap(
			StaticResource.class.getResourceAsStream("/mime.types"));

	private final long modified;
	private final long size;
	private final String contentType;
	private final URL url;

	public StaticResource(final long modified_, final long size_, final String contentType_, final URL url_) {
		modified = modified_;
		size = size_;
		url = url_;
		contentType = contentType_;
	}

	public StaticResource(final long modified_, final long size_, final URL url_) {
		modified = modified_;
		size = size_;
		url = url_;
		contentType = MIME_TYPES.getContentType(url_.getPath());
	}

	@Override
	public long modified() {
		return modified;
	}

	@Override
	public long size() {
		return size;
	}

	@Override
	public InputStream stream() throws IOException {
		return url.openStream();
	}

	@Override
	public String contentType() {
		return contentType;
	}

}