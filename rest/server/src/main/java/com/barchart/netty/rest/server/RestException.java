package com.barchart.netty.rest.server;

import io.netty.handler.codec.http.HttpResponseStatus;

public class RestException extends Exception {

	private static final long serialVersionUID = 1L;

	private HttpResponseStatus status =
			HttpResponseStatus.INTERNAL_SERVER_ERROR;

	public RestException() {
		super();
	}

	public RestException(final String message) {
		super(message);
	}

	public RestException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public RestException(final Throwable cause) {
		super(cause);
	}

	public RestException(final HttpResponseStatus status, final String message) {
		super(message);
		this.status = status;
	}

	public RestException(final HttpResponseStatus status, final String message,
			final Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	public HttpResponseStatus getStatus() {
		return status;
	}

}
