/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.rest.server;

import static org.junit.Assert.assertEquals;
import io.netty.handler.codec.http.HttpMethod;

import org.junit.Before;
import org.junit.Test;

public class TestRestHandler {

	private RestService service;
	private TestHandler handler;
	private TestRequest request;

	@Test
	public void testGet() throws Exception {

		service.handle(request);

		assertEquals(1, handler.requests);
		assertEquals(1, handler.get);
		assertEquals(0, handler.put);
		assertEquals(0, handler.post);
		assertEquals(0, handler.delete);
		assertEquals(0, handler.exceptions);

	}

	@Test
	public void testPut() throws Exception {

		request.method = HttpMethod.PUT;
		service.handle(request);

		assertEquals(1, handler.requests);
		assertEquals(0, handler.get);
		assertEquals(1, handler.put);
		assertEquals(0, handler.post);
		assertEquals(0, handler.delete);
		assertEquals(0, handler.exceptions);

	}

	@Test
	public void testPost() throws Exception {

		request.method = HttpMethod.POST;
		service.handle(request);

		assertEquals(1, handler.requests);
		assertEquals(0, handler.get);
		assertEquals(0, handler.put);
		assertEquals(1, handler.post);
		assertEquals(0, handler.delete);
		assertEquals(0, handler.exceptions);

	}

	@Test
	public void testDelete() throws Exception {

		request.method = HttpMethod.DELETE;
		service.handle(request);

		assertEquals(1, handler.requests);
		assertEquals(0, handler.get);
		assertEquals(0, handler.put);
		assertEquals(0, handler.post);
		assertEquals(1, handler.delete);
		assertEquals(0, handler.exceptions);

	}

	@Before
	public void setUp() throws Exception {

		service = new RestServiceBase();

		handler = new TestHandler("account");
		service.add("/{id}", handler);

		request = new TestRequest("/accounts/1234", "/1234", "1234");

	}

}
