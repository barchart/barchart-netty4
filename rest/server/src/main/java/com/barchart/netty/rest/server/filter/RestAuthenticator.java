package com.barchart.netty.rest.server.filter;

import rx.Observable;

import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.netty.server.http.request.RequestHandler;

public interface RestAuthenticator extends RequestHandler {

	/**
	 * The authentication type (BASIC, etc)
	 */
	public String type();

	/**
	 * Attempt to authenticate the request and return the authorized user ID, or
	 * null if authentication failed.
	 */
	public Observable<String> authenticate(String token,
			HttpServerRequest request);

}
