package com.barchart.netty.server.http;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.barchart.netty.server.AbstractServerBuilder;
import com.barchart.netty.server.http.error.DefaultErrorHandler;
import com.barchart.netty.server.http.error.ErrorHandler;
import com.barchart.netty.server.http.logging.NullRequestLogger;
import com.barchart.netty.server.http.logging.RequestLogger;
import com.barchart.netty.server.http.request.RequestHandler;
import com.barchart.netty.server.http.request.RequestHandlerFactory;

public class HttpServerBuilder extends
		AbstractServerBuilder<HttpServer, HttpServerBuilder> {

	protected Map<String, Object> handlers = new HashMap<String, Object>();
	protected int maxRequestSize = 1024 * 1024;
	protected boolean chunked = true;
	protected long timeout = 0;
	protected ErrorHandler errorHandler = new DefaultErrorHandler();
	protected RequestLogger requestLogger = new NullRequestLogger();

	/**
	 * Set the maximum request size in bytes (file uploads, etc). Defaults to
	 * 1048576 (1MB).
	 */
	public HttpServerBuilder maxRequestSize(final int max) {
		maxRequestSize = max;
		return this;
	}

	/**
	 * Set the default error handler.
	 */
	public HttpServerBuilder errorHandler(final ErrorHandler handler) {
		errorHandler = handler;
		return this;
	}

	/**
	 * Set the request logger.
	 */
	public HttpServerBuilder logger(final RequestLogger logger_) {
		requestLogger = logger_;
		return this;
	}

	/**
	 * Set whether the server should send large objects using chunked encoding.
	 * Defaults to true.
	 */
	public HttpServerBuilder chunked(final boolean chunked_) {
		chunked = chunked_;
		return this;
	}

	/**
	 * Set the maximum request time. Defaults to 0 (unlimited).
	 */
	public HttpServerBuilder timeout(final long timeout_) {
		timeout = timeout_;
		return this;
	}

	/**
	 * Add a request handler for the given prefix. Prefix matching is simplistic
	 * and the longest prefix that matches always wins regardless of number of
	 * path segments, so you should avoid overlapping handler prefixes when
	 * possible. For more complex handler routing using URL pattern matching see
	 * com.barchart.rest.Router.
	 * 
	 * Assuming two request handlers defined as:
	 * 
	 * <pre>
	 * requestHandler("/service", serviceHandler);
	 * requestHandler("/service/info", infoHandler);
	 * </pre>
	 * 
	 * A request to "/service/info/10" will go to infoHandler, but a request to
	 * "/service/something/else" will go to serviceHandler.
	 * 
	 * Inside the handler, ServerRequest.getPathInfo() will return the URL path
	 * portion that comes *after* the matched handler prefix. So, in the case of
	 * the examples above, getPathInfo() would return:
	 * 
	 * <pre>
	 * /service/info/10: "/10"
	 * /service/something/else: "/something/else"
	 * </pre>
	 */
	public HttpServerBuilder requestHandler(final String prefix,
			final RequestHandler handler) {
		handlers.put(prefix, handler);
		return this;
	}

	/**
	 * Add a request handler factory for the given prefix. This is similar to
	 * {@link HttpServerBuilder#requestHandler(String, RequestHandler)}, but
	 * allows greater control over the handler lifecycle for use cases like
	 * handler-per-connection or object pooling.
	 * 
	 * @see HttpServerBuilder#requestHandler(String, RequestHandler)
	 */
	public HttpServerBuilder requestHandler(final String prefix,
			final RequestHandlerFactory factory) {
		handlers.put(prefix, factory);
		return this;
	}

	/**
	 * Build a new HttpServer and immediately start listening to the given
	 * address.
	 */
	@Override
	public HttpServer listen(final SocketAddress address) {

		final HttpServer server = new HttpServer(new HttpServerConfig() //
				.address(address) //
				.childGroup(childGroup).parentGroup(parentGroup) //
				.maxConnections(maxConnections) //
				.maxRequestSize(maxRequestSize) //
				.errorHandler(errorHandler) //
				.logger(requestLogger) //
				.requestHandlers(handlers));

		server.listen();

		return server;

	}
}
