package com.barchart.netty.guice.http;

import com.barchart.netty.server.http.request.HttpServerRequest;

/**
 * Base HttpRequestHandler the provides no-op implementations of cancel() and release() to simplify basic handler
 * implementation.
 */
public abstract class AbstractHttpRequestHandler implements HttpRequestHandler {

	@Override
	public void cancel(final HttpServerRequest request) {
	}

	@Override
	public void release(final HttpServerRequest request) {
	}

}
