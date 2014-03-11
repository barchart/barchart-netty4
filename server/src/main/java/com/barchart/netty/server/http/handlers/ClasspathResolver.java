package com.barchart.netty.server.http.handlers;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class ClasspathResolver implements ResourceResolver {

	private final ClassLoader loader;
	private final String prefix;

	public ClasspathResolver(final Class<?> class_) {
		this(class_.getClassLoader(), class_.getPackage(), null);
	}

	public ClasspathResolver(final Class<?> class_, final String prefix_) {
		this(class_.getClassLoader(), class_.getPackage(), prefix_);
	}

	public ClasspathResolver(final ClassLoader loader_) {
		this(loader_, null, null);
	}

	public ClasspathResolver(final ClassLoader loader_, final String prefix_) {
		this(loader_, null, prefix_);
	}

	private ClasspathResolver(final ClassLoader loader_, final Package package_, final String prefix_) {

		if (loader_ == null)
			throw new IllegalArgumentException("Class cannot be null");

		loader = loader_;

		final StringBuilder path = new StringBuilder();

		if (package_ != null) {
			path.append(package_.getName().replace(".", "/")).append("/");
		}

		if (prefix_ != null) {
			if (prefix_.charAt(0) == '/')
				path.append(prefix_.substring(1));
			else
				path.append(prefix_);
		}

		if (path.length() > 0)
			path.append("/");

		prefix = path.toString();

	}

	@Override
	public Resource resolve(final String path) throws ResourceNotFoundException {

		final URL res = loader.getResource(prefix + (path.charAt(0) == '/' ? path.substring(1) : path));

		try {

			final URLConnection conn = res.openConnection();

			String contentType = conn.getContentType();

			if (contentType == null || "content/unknown".equals(contentType))
				contentType = StaticResource.MIME_TYPES.getContentType(path);

			return new StaticResource(conn.getLastModified(), conn.getContentLengthLong(), contentType, res);

		} catch (final IOException e) {
			StaticResourceHandler.log.debug("Resource could not be read: " + e);
		}

		throw new ResourceNotFoundException(path);

	}

}