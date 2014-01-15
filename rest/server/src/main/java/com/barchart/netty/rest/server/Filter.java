package com.barchart.netty.rest.server;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;

import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.netty.server.http.request.RequestHandler;
import com.barchart.netty.server.http.request.RequestHandlerBase;

public abstract class Filter<T extends Filter<T>> extends RequestHandlerBase {

	private RequestHandler next = null;

	public Filter() {
	}

	public Filter(final RequestHandler next_) {
		next = next_;
	}

	@SuppressWarnings("unchecked")
	public T setNext(final RequestHandler next_) {
		next = next_;
		return (T) this;
	}

	@Override
	public abstract void handle(final HttpServerRequest request)
			throws IOException;

	protected void next(final HttpServerRequest request) throws IOException {

		if (next != null) {
			try {
				next.handle(request);
			} catch (final Throwable t) {
				request.response().setStatus(
						HttpResponseStatus.INTERNAL_SERVER_ERROR);
				request.response().write("Error executing next handler");
				request.response().finish();
			}
		} else {
			request.response().setStatus(
					HttpResponseStatus.INTERNAL_SERVER_ERROR);
			request.response().write(
					"Filter.next() called with no next handler configured");
			request.response().finish();
		}

	}
}
