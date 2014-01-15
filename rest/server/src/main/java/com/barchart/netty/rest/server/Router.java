package com.barchart.netty.rest.server;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.barchart.netty.rest.server.impl.RoutedRequest;
import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.netty.server.http.request.RequestHandler;

/**
 * Request route for pluggable REST services.
 */
public class Router implements RequestHandler {

	private static final String ROOT = "";

	private final Map<URLPattern, HandlerMatcher> handlers;

	public Router() {
		handlers = new ConcurrentSkipListMap<URLPattern, HandlerMatcher>();
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
	 * @param pattern The URI pattern to match
	 * @param handler The handler that will process the request
	 * @throws IllegalStateException The same pattern was already registered
	 */
	public Router add(String pattern, final RequestHandler handler) {

		if (pattern != null && pattern.endsWith("/")) {
			pattern = pattern.substring(0, pattern.length() - 1);
		}

		if (pattern == null || pattern.isEmpty()) {
			pattern = ROOT;
		}

		final URLPattern key = new URLPattern(pattern);

		if (handlers.containsKey(key)) {
			throw new IllegalStateException("Route already defined: " + key);
		}

		handlers.put(key, new HandlerMatcher(pattern, handler));

		return this;

	}

	/**
	 * Remove a previously added route to this request.
	 * 
	 * @param pattern The URI pattern to match
	 */
	public Router remove(String pattern) {

		if (pattern != null && pattern.endsWith("/")) {
			pattern = pattern.substring(0, pattern.length() - 1);
		}

		if (pattern == null || pattern.isEmpty()) {
			pattern = ROOT;
		}

		handlers.remove(new URLPattern(pattern));

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
	 * Match all registered patterns against the given request URI.
	 * 
	 * @param uri The request URI
	 * @return Match details for a handler capable of servicing the request, or
	 *         null
	 */
	protected HandlerMatch match(final String uri) {

		for (final Map.Entry<URLPattern, HandlerMatcher> entry : handlers
				.entrySet()) {

			final HandlerMatch match = entry.getValue().match(uri);
			if (match != null) {
				return match;
			}

		}

		return null;

	}

	@Override
	public void handle(final HttpServerRequest request) throws IOException {

		final HandlerMatch match = match(uriSegment(request));

		if (match != null) {
			match.handler().handle(
					new RoutedRequest(request, match.prefix(), match.params()));
		} else {
			request.response().setStatus(HttpResponseStatus.NOT_FOUND);
			request.response().write("404 Not Found");
			request.response().finish();
		}

	}

	@Override
	public void cancel(final HttpServerRequest request) {

		final HandlerMatch match = match(uriSegment(request));

		if (match != null) {
			match.handler().cancel(
					new RoutedRequest(request, match.prefix(), match.params()));
		}

	}

	@Override
	public void release(final HttpServerRequest request) {

		final HandlerMatch match = match(uriSegment(request));

		if (match != null) {
			match.handler().release(
					new RoutedRequest(request, match.prefix(), match.params()));
		}

	}

	private String uriSegment(final HttpServerRequest request) {
		return request.getPathInfo();
	}

	protected static class HandlerMatcher {

		private static final Pattern PARAM_PATTERN = Pattern
				.compile("\\{[^\\/]+\\}");
		private static final String TARGET_PARAM = "([^\\/]+)";

		private final String pattern;
		private final String prefix;
		private final Pattern compiled;
		private final List<String> params;
		private final RequestHandler handler;

		protected HandlerMatcher(final String pattern_,
				final RequestHandler handler_) {

			pattern = pattern_;
			handler = handler_;

			if (pattern.contains("{")) {

				// Has URI path params
				params = new ArrayList<String>();
				prefix = pattern.substring(0, pattern.indexOf('{'));

				final StringBuffer sb = new StringBuffer();
				final Matcher matcher = PARAM_PATTERN.matcher(pattern);
				while (matcher.find()) {
					// Find parameter name
					final String name = matcher.group();
					params.add(name.substring(1, name.length() - 1));
					// Replace with regex pattern for matching requests
					matcher.appendReplacement(sb, TARGET_PARAM);
				}
				matcher.appendTail(sb);

				compiled = Pattern.compile("^" + sb.toString());

			} else {

				// Static path, no params
				prefix = pattern;
				compiled = null;
				params = null;

			}

		}

		protected HandlerMatch match(String uri) {

			if (uri.endsWith("/")) {
				uri = uri.substring(0, uri.length() - 1);
			}

			if (compiled == null) {

				// Root handler is special, don't want it to match everything
				// unknown
				if (ROOT.equals(pattern)) {
					if (ROOT.equals(uri)) {
						return new HandlerMatch(handler, null, pattern);
					}
				} else if (uri.startsWith(pattern)) {
					return new HandlerMatch(handler, null, pattern);
				}

			} else if (uri.startsWith(prefix)) {

				final Matcher matcher = compiled.matcher(uri);

				if (matcher.find()) {

					final Map<String, String> uriParams =
							new HashMap<String, String>();

					for (int i = 0; i < matcher.groupCount(); i++) {
						try {
							uriParams.put(params.get(i), URLDecoder.decode(
									matcher.group(i + 1), "UTF-8"));
						} catch (final UnsupportedEncodingException e) {
							throw new RuntimeException(e);
						}
					}

					return new HandlerMatch(handler, uriParams, matcher.group());

				}

			}

			return null;

		}
	}

	/**
	 * An pre-processed URI match including parsed URI parameter values
	 */
	protected static class HandlerMatch {

		private final RequestHandler handler;
		private final Map<String, String> params;
		private final String prefix;

		protected HandlerMatch(final RequestHandler handler_,
				final Map<String, String> params_, final String prefix_) {
			handler = handler_;
			params = params_;
			prefix = prefix_;
		}

		protected RequestHandler handler() {
			return handler;
		}

		protected Map<String, String> params() {
			return params;
		}

		protected String prefix() {
			return prefix;
		}

	}

	/**
	 * Sorts URL patterns by number of path segments, whether or not they
	 * contain embedded parameters, and by pattern length.
	 * 
	 * Example: 2 handlers are defined as:
	 * 
	 * <pre>
	 * add("/service", handler1);
	 * add("/service/info", handler2);
	 * add("/service/{id}", handler3);
	 * </pre>
	 * 
	 * Routing for the following requet URIs would be:
	 * 
	 * <pre>
	 * /service => handler1
	 * /service/123 => handler 3 (dynamic parameters)
	 * /service/info => handler2 (static URLs take priority over params)
	 * </pre>
	 */
	protected static class URLPattern implements Comparable<URLPattern> {

		private final String[] segments;
		private final String pattern;
		private final boolean isStatic;

		public URLPattern(final String pattern_) {
			pattern = pattern_;
			segments = pattern.split("/");
			isStatic =
					!HandlerMatcher.PARAM_PATTERN.matcher(
							segments[segments.length - 1]).matches();
		}

		@Override
		public int compareTo(final URLPattern o) {

			// Patterns with more path segments should be matched first
			if (segments.length < o.segments.length) {
				return 1;
			} else if (segments.length > o.segments.length) {
				return -1;
			}

			// Patterns with same number of segments should match static paths
			// first
			if (isStatic && !o.isStatic) {
				return -1;
			} else if (!isStatic && o.isStatic) {
				return 1;
			}

			// Finally compare pattern length if everything matches
			final int l1 = pattern.length();
			final int l2 = o.pattern.length();

			if (l1 < l2) {
				return 1;
			} else if (l2 < l1) {
				return -1;
			}

			return pattern.compareTo(o.pattern);

		}

		@Override
		public String toString() {
			if (pattern != null) {
				return pattern;
			}
			return "";
		}

		@Override
		public boolean equals(final Object o) {
			if (pattern == null) {
				return o == null;
			}
			return pattern.equals(o.toString());
		}

		@Override
		public int hashCode() {
			if (pattern != null) {
				return pattern.hashCode();
			}
			return 0;
		}

	}

}
