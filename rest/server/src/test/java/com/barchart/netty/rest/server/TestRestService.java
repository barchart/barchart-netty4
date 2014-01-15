package com.barchart.netty.rest.server;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestRestService {

	@Test
	public void testService() throws Exception {

		final RestService service = new RestServiceBase();

		final TestHandler account = new TestHandler("account");
		service.add("/{id}", account);

		service.handle(new TestRequest("/accounts/1234", "/1234", "1234"));

		assertEquals(1, account.requests);
		assertEquals(0, account.exceptions);

	}

}
