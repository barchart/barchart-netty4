package com.barchart.netty.server.http.error;

import io.netty.handler.codec.http.HttpResponseStatus;

public class ServerTooBusyException extends ServerException {

	private static final long serialVersionUID = 1L;

	public ServerTooBusyException() {
		super(HttpResponseStatus.SERVICE_UNAVAILABLE);
	}

	public ServerTooBusyException(final String message) {
		super(HttpResponseStatus.SERVICE_UNAVAILABLE, message);
	}

	public ServerTooBusyException(final String message, final Throwable cause) {
		super(HttpResponseStatus.SERVICE_UNAVAILABLE, message, cause);
	}

	public ServerTooBusyException(final Throwable cause) {
		super(HttpResponseStatus.SERVICE_UNAVAILABLE, cause);
	}

}
