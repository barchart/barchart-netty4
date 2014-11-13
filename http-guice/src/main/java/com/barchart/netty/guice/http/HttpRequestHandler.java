package com.barchart.netty.guice.http;

import com.barchart.netty.server.http.request.RequestHandler;

/**
 * OSGI injectable HTTP request handler.
 */
public interface HttpRequestHandler extends RequestHandler {

	/**
	 * Return the path that this handler should be registered at.
	 */
	public String path();

}
