package com.barchart.netty.server.http.handlers;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.Date;

import javax.activation.MimetypesFileTypeMap;

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

	private static final Logger log = LoggerFactory.getLogger(StaticResourceHandler.class);

	public static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap(
			StaticResourceHandler.class.getResourceAsStream("/mime.types"));

	private final ResourceResolver resolver;

	public StaticResourceHandler(final File directory_) {
		this(new DirectoryResolver(directory_));
	}

	public StaticResourceHandler(final Class<?> class_) {
		this(new ClasspathResolver(class_));
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

	private static class StaticResourceImpl implements Resource {

		private final long modified;
		private final long size;
		private final String contentType;
		private final URL url;

		StaticResourceImpl(final long modified_, final long size_, final String contentType_, final URL url_) {
			modified = modified_;
			size = size_;
			url = url_;
			contentType = contentType_;
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

	public static class DirectoryResolver implements ResourceResolver {

		private final File directory;

		public DirectoryResolver(final File directory_) {
			if (directory_ == null || !directory_.exists() || !directory_.isDirectory())
				throw new IllegalArgumentException("Not a directory: " + directory_);
			directory = directory_;
		}

		@Override
		public Resource resolve(final String path) throws ResourceNotFoundException {
			final File file = new File(directory, path);
			if (file.exists()) {
				try {
					return new StaticResourceImpl(file.lastModified(), file.length(),
							MIME_TYPES.getContentType(file), file.toURI().toURL());
				} catch (final MalformedURLException e) {
				}
			}
			throw new ResourceNotFoundException(file.getAbsolutePath());
		}

	}

	public static class ClasspathResolver implements ResourceResolver {

		private final ClassLoader loader;
		private final String prefix;

		public ClasspathResolver(final Class<?> class_) {
			this(class_.getClassLoader(),
					class_.getPackage().getName().replace(".", "/"));
		}

		public ClasspathResolver(final ClassLoader loader_, final String prefix_) {
			if (loader_ == null)
				throw new IllegalArgumentException("Class cannot be null");
			loader = loader_;
			if (prefix_.charAt(0) == '/')
				prefix = prefix_.substring(1);
			else
				prefix = prefix_;
		}

		@Override
		public Resource resolve(final String path) throws ResourceNotFoundException {
			final URL res = loader.getResource(prefix + "/" + (path.charAt(0) == '/' ? path.substring(1) : path));
			try {
				final URLConnection conn = res.openConnection();
				String contentType = conn.getContentType();
				if (contentType == null || "content/unknown".equals(contentType))
					contentType = MIME_TYPES.getContentType(path);
				return new StaticResourceImpl(conn.getLastModified(), conn.getContentLengthLong(), contentType, res);
			} catch (final IOException e) {
				log.debug("Resource could not be read: " + e);
			}
			throw new ResourceNotFoundException(path);
		}

	}

}
