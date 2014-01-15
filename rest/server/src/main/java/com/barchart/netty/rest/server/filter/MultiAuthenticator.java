package com.barchart.netty.rest.server.filter;

import io.netty.handler.codec.http.HttpHeaders;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Observer;

import com.barchart.netty.server.http.request.HttpServerRequest;

public class MultiAuthenticator extends
		RestAuthenticatorBase<MultiAuthenticator> {

	private final Map<String, RestAuthenticator> authenticators =
			new HashMap<String, RestAuthenticator>();

	public MultiAuthenticator(final RestAuthenticator... authenticators_) {
		for (final RestAuthenticator a : authenticators_) {
			add(a);
		}
	}

	public MultiAuthenticator add(final RestAuthenticator authenticator) {
		authenticators.put(authenticator.type().toUpperCase(), authenticator);
		return this;
	}

	@Override
	public final void handle(final HttpServerRequest request)
			throws IOException {

		try {

			if (!request.headers().contains(HttpHeaders.Names.AUTHORIZATION)) {
				unauthorized(request.response(), "Authorization required");
				return;
			}

			final String[] authorization =
					request.headers().get(HttpHeaders.Names.AUTHORIZATION)
							.split(" ", 2);

			if (authorization.length != 2) {
				unauthorized(request.response(), "Authorization failed");
				return;
			}

			final String type = authorization[0].toUpperCase();

			if (!authenticators.containsKey(type)) {
				unauthorized(request.response(),
						"Unsupported authorization type: " + authorization[0]);
				return;
			}

			authenticators.get(type).authenticate(authorization[1], request)
					.subscribe(new Observer<String>() {

						String authId = null;

						@Override
						public void onCompleted() {

							try {
								if (authId != null) {
									// response.setStatus(HttpResponseStatus.ACCEPTED);
									next(wrap(request, authId));
								} else {
									unauthorized(request.response(),
											"Authorization failed");
								}
							} catch (final IOException ioe) {
							}

						}

						@Override
						public void onError(final Throwable e) {

							try {
								unauthorized(request.response(), e.getMessage());
							} catch (final IOException ioe) {
							}

						}

						@Override
						public void onNext(final String authId_) {

							authId = authId_;

						}

					});

		} catch (final Throwable t) {

			unauthorized(request.response(), t.getMessage());

		}

	}

	@Override
	public String type() {
		return null;
	}

	@Override
	public Observable<String> authenticate(final String token,
			final HttpServerRequest request) {
		return Observable.error(new UnsupportedOperationException());
	}

}
