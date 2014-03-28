/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.handlers;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.netty.server.http.request.RequestHandlerBase;

/**
 * Static resource serving handler.
 *
 * Send static resources (images, CSS, etc) to the requestor from the specified
 * resource root. Resource roots can be a filesystem directory or a classpath
 * location;
 */
public class StaticResourceHandler extends RequestHandlerBase {

	static final Logger log = LoggerFactory.getLogger(StaticResourceHandler.class);

	private final ResourceResolver resolver;

	public StaticResourceHandler(final File directory_) {
		this(new DirectoryResolver(directory_));
	}

	public StaticResourceHandler(final Class<?> class_, final String prefix_) {
		this(new ClasspathResolver(class_, prefix_));
	}

	public StaticResourceHandler(final ClassLoader loader_, final String prefix_) {
		this(new ClasspathResolver(loader_, prefix_));
	}

	public StaticResourceHandler(final ResourceResolver resolver_) {
		resolver = resolver_;
	}

	@Override
	public void handle(final HttpServerRequest request) throws IOException {

		try {

			final Resource resource = resolver.resolve(request.getPathInfo());

			if (resource.contentType() != null)
				request.response().setContentType(resource.contentType());

			// Set Cache-Control: public
			request.response().headers().add(HttpHeaders.Names.CACHE_CONTROL,
					HttpHeaders.Values.PUBLIC);

			if (!handleCache(request, resource)) {

				// Set modification time
				HttpHeaders.setDateHeader(request.response(), HttpHeaders.Names.LAST_MODIFIED,
						new Date(resource.modified()));

				request.response().setContentLength((int) resource.size());

				// Chunked response for (potentially) large files
				if (resource.size() == 0 || resource.size() > 8192)
					request.response().setChunkSize(8192);

				IOUtils.copy(resource.stream(), request.response().getOutputStream());

			}

		} catch (final IOException e) {

			request.response().setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
			request.response().write("500 Server Error");

		} catch (final ResourceNotFoundException e) {

			request.response().setStatus(HttpResponseStatus.NOT_FOUND);
			request.response().write("404 Not Found");

		} finally {

			request.response().finish();

		}

	}

	private boolean handleCache(final HttpServerRequest request, final Resource resource) {

		// Cache-Control: no-cache
		if (request.headers().contains(HttpHeaders.Names.CACHE_CONTROL)
				&& request.headers().get(HttpHeaders.Names.CACHE_CONTROL).equals(HttpHeaders.Values.NO_CACHE)) {
			return false;
		}

		// If-Modified-Since: <date>
		if (request.headers().contains(HttpHeaders.Names.IF_MODIFIED_SINCE)) {

			try {

				final Date cached = HttpHeaders.getDateHeader(request, HttpHeaders.Names.IF_MODIFIED_SINCE);

				if (cached.getTime() >= resource.modified()) {
					// Cache is good, return 304
					request.response().setStatus(HttpResponseStatus.NOT_MODIFIED);
					request.response().setContentLength(0);
					return true;
				}

			} catch (final ParseException e) {
				// Bad date header
			}

		}

		// Send resource
		return false;

	}

}
