package com.barchart.netty.rest.server;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observer;

import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.netty.server.http.request.HttpServerResponse;
import com.barchart.netty.server.http.request.RequestHandlerBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base handler for REST request processing. Subclasses should override the HTTP
 * methods that they support (get(), post(), put(), delete()).
 */
public class RestHandlerBase extends RequestHandlerBase {

	private static final ObjectMapper mapper = new ObjectMapper();

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public void get(final HttpServerRequest request) throws IOException {
		complete(request.response(), HttpResponseStatus.METHOD_NOT_ALLOWED,
				"GET not implemented");
	}

	public void post(final HttpServerRequest request) throws IOException {
		complete(request.response(), HttpResponseStatus.METHOD_NOT_ALLOWED,
				"POST not implemented");
	}

	public void put(final HttpServerRequest request) throws IOException {
		complete(request.response(), HttpResponseStatus.METHOD_NOT_ALLOWED,
				"PUT not implemented");
	}

	public void delete(final HttpServerRequest request) throws IOException {
		complete(request.response(), HttpResponseStatus.METHOD_NOT_ALLOWED,
				"DELETE not implemented");
	}

	@Override
	public void handle(final HttpServerRequest request) throws IOException {

		final HttpMethod method = request.getMethod();

		try {
			if (method == HttpMethod.GET) {
				get(request);
			} else if (method == HttpMethod.POST) {
				post(request);
			} else if (method == HttpMethod.PUT) {
				put(request);
			} else if (method == HttpMethod.DELETE) {
				delete(request);
			} else {
				complete(request.response(),
						HttpResponseStatus.METHOD_NOT_ALLOWED, method.name()
								+ " not implemented");
			}
		} catch (final Throwable t) {
			complete(request.response(),
					HttpResponseStatus.INTERNAL_SERVER_ERROR, t.getMessage());
		}

	}

	/**
	 * Verify that the specified parameters are provided in the request. If they
	 * are missing, send an error response.
	 * 
	 * @param params The list of required parameters
	 * @return True if parameters exist, false if an error response was sent
	 */
	protected static boolean requireParams(final HttpServerRequest request,
			final String... params) {

		for (final String param : params) {
			final String value = request.getParameter(param);
			if (value == null || value.isEmpty()) {
				complete(request.response(), HttpResponseStatus.BAD_REQUEST,
						param + " is invalid or not provided");
				return false;
			}
		}

		return true;

	}

	/**
	 * Send the given response and close the connection.
	 */
	protected static void complete(final HttpServerResponse response,
			final HttpResponseStatus status, final String message) {
		try {
			response.setStatus(status);
			if (message != null) {
				response.write(message);
			}
		} catch (final IOException e) {
		} finally {
			try {
				response.finish();
			} catch (final IOException e) {
			}
		}
	}

	protected static void writeJson(final HttpServerResponse response,
			final Object value, final boolean finish) {
		try {
			response.headers().set(HttpHeaders.Names.CONTENT_TYPE,
					"application/json");
			response.setStatus(HttpResponseStatus.OK);
			response.write(mapper.writeValueAsBytes(value));
		} catch (final JsonProcessingException jpe) {
		} catch (final IOException ioe) {
		} finally {
			if (finish) {
				try {
					response.finish();
				} catch (final IOException e) {
				}
			}
		}
	}

	protected ObjectMapper mapper() {
		return RestHandlerBase.mapper;
	}

	/**
	 * Utility observer class that outputs a "200 OK" status and message when
	 * onComplete() is called and finishes the response. If onError() is called,
	 * a 500 status is returned.
	 * 
	 * @param <T> The callback type
	 */
	protected static class BasicResponder<T> implements Observer<T> {

		protected final Logger log = LoggerFactory.getLogger(getClass());

		protected final HttpServerResponse response;
		protected final String message;

		private final List<T> results;

		/**
		 * @param response_ The ServerReponse for this request
		 */
		public BasicResponder(final HttpServerResponse response_) {
			this(response_, "OK");
		}

		/**
		 * @param response_ The ServerReponse for this request
		 */
		public BasicResponder(final HttpServerResponse response_,
				final String message_) {
			response = response_;
			message = message_;
			results = new ArrayList<T>();
		}

		@Override
		public void onNext(final T next) {
			results.add(next);
		}

		@Override
		public void onError(final Throwable error) {

			if (error instanceof RestException) {
				final RestException re = (RestException) error;
				complete(response, re.getStatus(), re.getMessage());
			} else {
				log.debug("Unhandled exception during response", error);
				complete(response, HttpResponseStatus.INTERNAL_SERVER_ERROR,
						error.getMessage());
			}

		}

		@Override
		public void onCompleted() {
			complete(response, HttpResponseStatus.OK, message);
		}

		public List<T> results() {
			return results;
		}

		public T result() {
			if (results.size() > 0) {
				return results.get(0);
			}
			return null;
		}

	}

	/**
	 * Utility observer class that outputs any data received from an Observable
	 * as a serialized JSON object (or list of objects). If onError() is called,
	 * a 500 status is returned.
	 * 
	 * @param <T> The callback type
	 */
	protected static class JsonResponder<T> extends BasicResponder<T> {

		private final boolean list;
		private final boolean required;

		/**
		 * @param response_ The ServerReponse for this request
		 * @param list_ Output all onNext() objects as a list, or just serialize
		 *            the first item
		 */
		public JsonResponder(final HttpServerResponse response_,
				final boolean list_) {
			this(response_, list_, false);
		}

		public JsonResponder(final HttpServerResponse response_,
				final boolean list_, final boolean required_) {
			super(response_);
			list = list_;
			required = required_;
		}

		@Override
		public void onCompleted() {

			if (required && results().size() == 0) {
				complete(response, HttpResponseStatus.NOT_FOUND,
						"Object not found");
			} else {
				if (list) {
					writeJson(response, results(), true);
				} else {
					writeJson(response, result(), true);
				}
			}

		}

	}

}
