package com.barchart.netty.rest.server;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.rest.server.impl.RoutedRequest;
import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.netty.server.http.request.RequestHandler;

/**
 * Request route for pluggable REST services.
 */
public class Router implements RequestHandler {

	private final static Logger log = LoggerFactory.getLogger(Router.class);

	private final Map<RestEndpoint, RequestHandler> handlers;

	public Router() {
		handlers = new ConcurrentSkipListMap<RestEndpoint, RequestHandler>();
	}

	/**
	 * Add a route to this Router. URI patterns must match the entire request
	 * URI. You can specify named parameters in the URI pattern, which will be
	 * parsed out and added to the ServerRequest.getParameters() values
	 * available to the REST handler.
	 *
	 * Trailing slashes are stripped when matching URIs, so the following
	 * patterns are equivalent:
	 *
	 * <pre>
	 * /account/create
	 * /account/create/
	 * </pre>
	 *
	 * Examples:
	 *
	 * <pre>
	 * router.add("/account", new AccountListHandler());
	 * router.add("/account/create", new CreateAccountHandler());
	 * router.add("/account/{id}", new AccountHandler());
	 * router.add("/account/{id}/orders", new OrdersHandler());
	 * router.add("/account/{id}/orders/{order}", new OrderHandler());
	 * </pre>
	 *
	 * For a request to "/account/1234", the AccountHandler instance would
	 * handle the request, and the user ID would be available to the handler via
	 * ServerRequest.getParameter("id");
	 * 
	 * To match the root path of a router, use "" or "/" as the pattern.
	 *
	 * @param pattern The URI pattern to match
	 * @param handler The handler that will process the request
	 * @throws IllegalStateException The same pattern was already registered
	 */
	public Router add(String pattern, final RequestHandler handler) {

		pattern = trim(pattern);

		if (pattern == null || pattern.isEmpty()) {
			pattern = RestEndpoint.ROOT;
		}

		return add(new RestEndpoint(pattern), handler);

	}

	/**
	 * @see Router#add(String, RequestHandler)
	 */
	public Router add(final RestEndpoint endpoint, final RequestHandler handler) {

		if (handlers.containsKey(endpoint)) {
			throw new IllegalStateException("Route already defined: " + endpoint);
		}

		handlers.put(endpoint, handler);

		return this;

	}

	/**
	 * Remove a previously added route to this request.
	 *
	 * @param pattern The URI pattern to match
	 */
	public Router remove(String pattern) {

		pattern = trim(pattern);

		if (pattern == null || pattern.isEmpty()) {
			pattern = RestEndpoint.ROOT;
		}

		return remove(new RestEndpoint(pattern));

	}

	/**
	 * Remove a previously added route to this request.
	 *
	 * @param endpoint The REST endpoint to match
	 */
	public Router remove(final RestEndpoint endpoint) {

		handlers.remove(endpoint);

		return this;

	}

	/**
	 * Remove all routes.
	 */
	public Router clear() {
		handlers.clear();
		return this;
	}

	/**
	 * Trim trailing slashes from the URI, we don't distinguish between the two
	 * for routing.
	 */
	private String trim(final String uri) {
		if (uri != null && uri.endsWith("/"))
			return uri.substring(0, uri.length() - 1);
		return uri;
	}

	/**
	 * Attempt to route the given request to a handler, wrapping the request for
	 * proper path info resolution.
	 *
	 * To handle a request:
	 *
	 * route(request).handle();
	 *
	 * To cancel a running request:
	 *
	 * route(request).cancel();
	 *
	 * @return Route info for a handler capable of servicing the request
	 * @throws RestException if no route could be found for this request
	 */
	public RouteHandler route(final HttpServerRequest request) throws RestException {

		final String uri = trim(request.getPathInfo());

		for (final Map.Entry<RestEndpoint, RequestHandler> entry : handlers.entrySet()) {

			try {

				if (entry.getKey().match(uri)) {

					final RestEndpoint.Parsed parsed = entry.getKey().parse(uri);
					final HttpServerRequest routed = new RoutedRequest(request, parsed.match(), parsed.params());

					return new RouteHandler(entry.getValue(), routed);

				}

			} catch (final ParseException p) {
				log.warn("Matched route but was unable to parse: " + uri);
			}

		}

		throw new RestException("No route could be found for request: " + uri);

	}

	@Override
	public void handle(final HttpServerRequest request) throws IOException {

		try {
			route(request).handle();
		} catch (final RestException e) {
			request.response().setStatus(HttpResponseStatus.NOT_FOUND);
			request.response().write("404 Not Found");
			request.response().finish();
		}

	}

	@Override
	public void cancel(final HttpServerRequest request) {

		try {
			route(request).cancel();
		} catch (final RestException e) {
		}

	}

	@Override
	public void release(final HttpServerRequest request) {

		try {
			route(request).release();
		} catch (final RestException e) {
		}

	}

	/**
	 * A route handler for a specific request, which can be used to dispatch
	 * requests to handle(), cancel() or release().
	 */
	public static class RouteHandler {

		final RequestHandler handler;
		final HttpServerRequest request;

		protected RouteHandler(final RequestHandler handler_,
				final HttpServerRequest request_) {
			handler = handler_;
			request = request_;
		}

		public void handle() throws IOException {
			handler.handle(request);
		}

		public void cancel() {
			handler.cancel(request);
		}

		public void release() {
			handler.release(request);
		}

	}

}
