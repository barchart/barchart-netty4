package com.barchart.netty.rest.server;

import java.io.IOException;

import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.netty.server.http.request.RequestHandler;
import com.barchart.netty.server.http.request.RequestHandlerBase;

/**
 * Root module for REST services. Uses a Router internally for request
 * processing. Subclass to create independent OSGI-enabled service modules that
 * have multiple internal REST service endpoints.
 * 
 * @author jeremy
 * 
 */
public class RestServiceBase extends RequestHandlerBase implements RestService {

	private final Router router = new Router();

	@Override
	public void add(final String pattern, final RequestHandler handler) {
		router.add(pattern, handler);
	}

	@Override
	public void remove(final String pattern) {
		router.remove(pattern);
	}

	@Override
	public void handle(final HttpServerRequest request) throws IOException {
		router.handle(request);
	}

	@Override
	public void cancel(final HttpServerRequest request) {
		router.cancel(request);
	}

	@Override
	public void release(final HttpServerRequest request) {
		router.release(request);
	}

}
