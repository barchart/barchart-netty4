package com.barchart.netty.rest.server.example;

import java.io.IOException;

import rx.Observable;

import com.barchart.netty.rest.server.RestServiceBase;
import com.barchart.netty.rest.server.filter.RestAuthenticatorBase;
import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.netty.server.http.request.RequestHandlerBase;

/**
 * Example of a REST module that registers multiple sub-handlers for routing.
 * This would be registered with the core netty-http server (preferably
 * automatically via barchart-osgi-http).
 * 
 * @author jeremy
 */
public class Example extends RestServiceBase {

	// Root path for this REST module
	private final String PATH = "/example";

	public void configure() {

		// Simple service with no path params or auth
		add(PATH + "/auth", new DummyHandler());

		// Service using an authenticator filter
		add(PATH + "/account",
				new DummyAuthenticator().setNext(new DummyHandler()));

		// Service using path parameters - will be injected into
		// request.getParameters().
		add(PATH + "/account/{id}", new DummyHandler());

		add(PATH + "/find/{id}", new DummyHandler());
		add(PATH + "/reset/{id}", new DummyHandler());
		add(PATH + "/permissions/{id}", new DummyHandler());

		// Multiple path parameters
		add(PATH + "/permissions/{id}/{prefix}", new DummyHandler());

	}

	public String getPath() {
		return PATH;
	}

	private class DummyHandler extends RequestHandlerBase {
		@Override
		public void handle(final HttpServerRequest request) throws IOException {
		}
	}

	private class DummyAuthenticator extends
			RestAuthenticatorBase<DummyAuthenticator> {

		@Override
		public String type() {
			return "DUMMY";
		}

		@Override
		public Observable<String> authenticate(final String token,
				final HttpServerRequest request) {
			return Observable.error(new UnsupportedOperationException());
		}

	}

}