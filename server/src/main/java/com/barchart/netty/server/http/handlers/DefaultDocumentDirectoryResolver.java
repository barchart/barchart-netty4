package com.barchart.netty.server.http.handlers;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DefaultDocumentDirectoryResolver implements ResourceResolver {

	private static final List<String> DEFAULT_DOCUMENT_NAMES = Collections.unmodifiableList(Arrays.asList(new String[] { "index.html", "index.htm" }));

	private final File directory;

	private final List<String> defaultDocumentNames;

	public DefaultDocumentDirectoryResolver(File directory_) {
		this(directory_, DEFAULT_DOCUMENT_NAMES);
	}

	public DefaultDocumentDirectoryResolver(File directory_, List<String> defaultDocumentNames_) {
		if (directory_ == null || !directory_.exists() || !directory_.isDirectory())
			throw new IllegalArgumentException("Not a directory: " + directory_);
		if (defaultDocumentNames_ == null || defaultDocumentNames_.isEmpty())
			throw new IllegalArgumentException("Bad list of default document names: " + defaultDocumentNames_);
		directory = directory_;
		defaultDocumentNames = new ArrayList<String>(defaultDocumentNames_);
	}

	@Override
	public Resource resolve(String path) throws ResourceNotFoundException {
		File file = new File(directory, path);
		if (file.isDirectory()) {
			file = findDefaultDocument(file);
		}
		return resolveFile(file);
	}

	private File findDefaultDocument(File directory) throws ResourceNotFoundException {
		for (String fileName : defaultDocumentNames) {
			File newFile = new File(directory, fileName);
			if (newFile.exists() && !newFile.isDirectory()) {
				return newFile;
			}
		}
		throw new ResourceNotFoundException(directory.getAbsolutePath());
	}

	private Resource resolveFile(File file) throws ResourceNotFoundException {
		if (file.exists()) {
			try {
				return new StaticResource(file.lastModified(), file.length(), StaticResource.MIME_TYPES.getContentType(file), file.toURI().toURL());
			} catch (final MalformedURLException e) {
			}
		}
		throw new ResourceNotFoundException(file.getAbsolutePath());

	}

}
