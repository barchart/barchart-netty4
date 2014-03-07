package com.barchart.netty.server.http.handlers;

import java.io.File;
import java.net.MalformedURLException;

public class DirectoryResolver implements ResourceResolver {

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
				return new StaticResource(file.lastModified(), file.length(),
						StaticResource.MIME_TYPES.getContentType(file), file.toURI().toURL());
			} catch (final MalformedURLException e) {
			}
		}
		throw new ResourceNotFoundException(file.getAbsolutePath());
	}

}