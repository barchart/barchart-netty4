package com.barchart.netty.server.http.error;

import io.netty.handler.codec.http.HttpResponseStatus;

public class ServerException extends Exception {

	private static final long serialVersionUID = 1L;

	private HttpResponseStatus status = HttpResponseStatus.OK;

	public ServerException(final HttpResponseStatus status_) {
		super();
		status = status_;
	}

	public ServerException(final HttpResponseStatus status_,
			final String message_) {
		super(message_);
		status = status_;
	}

	public ServerException(final HttpResponseStatus status_,
			final Throwable cause_) {
		super(cause_);
		status = status_;
	}

	public ServerException(final HttpResponseStatus status_,
			final String message_, final Throwable cause_) {
		super(message_, cause_);
		status = status_;
	}

	public HttpResponseStatus getStatus() {
		return status;
	}

}
