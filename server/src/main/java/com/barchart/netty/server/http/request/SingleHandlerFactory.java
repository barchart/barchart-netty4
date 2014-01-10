package com.barchart.netty.server.http.request;

public class SingleHandlerFactory implements
		RequestHandlerFactory {

	private final RequestHandler handler;

	public SingleHandlerFactory(final RequestHandler handler_) {
		handler = handler_;
	}

	@Override
	public RequestHandler newHandler(final HttpServerRequest request) {
		return handler;
	}

}