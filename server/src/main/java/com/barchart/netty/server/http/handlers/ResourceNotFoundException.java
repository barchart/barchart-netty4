package com.barchart.netty.server.http.handlers;

public class ResourceNotFoundException extends Exception {

	public String resource;

	ResourceNotFoundException(final String resource_) {
		resource = resource_;
	}

	@Override
	public String getMessage() {
		return resource + " not found";
	}

	public String getResource() {
		return resource;
	}

}