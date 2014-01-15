package com.barchart.netty.rest.server.filter;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;

import rx.Observer;

import com.barchart.netty.rest.server.Filter;
import com.barchart.netty.rest.server.impl.RoutedRequest;
import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.netty.server.http.request.HttpServerResponse;

public abstract class RestAuthenticatorBase<T extends RestAuthenticatorBase<T>>
		extends Filter<T> implements RestAuthenticator {

	@Override
	public void handle(final HttpServerRequest request) throws IOException {

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

			if (!type.equals(type())) {
				unauthorized(request.response(),
						"Unsupported authorization type: " + authorization[0]);
				return;
			}

			authenticate(authorization[1], request).subscribe(
					new Observer<String>() {

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

	/**
	 * Return a 401 Unauthorized message to the client.
	 */
	protected void unauthorized(final HttpServerResponse response,
			final String message) throws IOException {
		response.headers().set(HttpHeaders.Names.WWW_AUTHENTICATE,
				"Basic realm=\"barchart.com\"");
		response.setStatus(HttpResponseStatus.UNAUTHORIZED);
		response.write(message);
		response.finish();
	}

	/**
	 * Wraps the request in a request instance that returns the specified remote
	 * user.
	 * 
	 * @param request The original request
	 * @param remoteUser The authenticated user ID / name
	 */
	protected HttpServerRequest wrap(final HttpServerRequest request,
			final String remoteUser) {
		if (request instanceof RoutedRequest) {
			((RoutedRequest) request).setRemoteUser(remoteUser);
			return request;
		} else {
			final RoutedRequest wrapper = new RoutedRequest(request, null);
			wrapper.setRemoteUser(remoteUser);
			return wrapper;
		}
	}

}
