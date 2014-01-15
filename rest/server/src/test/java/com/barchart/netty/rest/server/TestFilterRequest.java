package com.barchart.netty.rest.server;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.barchart.netty.server.http.request.HttpServerRequest;

public class TestFilterRequest {

	private RestService service;
	private TestHandler handler;
	private TestRequest request;

	@Test
	public void testFilter() throws Exception {

		service.handle(request);

		assertEquals(1, handler.requests);
		assertEquals(0, handler.exceptions);

	}

	@Before
	public void setUp() throws Exception {

		service = new RestServiceBase();

		handler = new TestHandler("account");
		service.add("/{id}", new TestFilter().setNext(handler));

		request = new TestRequest("/accounts/1234", "/1234", "1234");

	}

	public static class TestFilter extends Filter<TestFilter> {

		@Override
		public void handle(final HttpServerRequest request) throws IOException {
			next(request);
		}

	}

}
