package com.barchart.netty.rest.client;

import static org.junit.Assert.*;

import org.junit.Test;

import com.barchart.netty.rest.client.RestRequest.Method;
public class TestRestRequest {

	@Test
	public void testEquals() {

		// Comparison request
		final RestRequest req = new RestRequest(Method.GET, "http://localhost/req");
		req.param("param", "value");
		req.header("Header", "text");

		final RestRequest reqMatch = new RestRequest(Method.GET, "http://localhost/req");
		reqMatch.param("param", "value");
		reqMatch.header("Header", "text");
		match(req, reqMatch);

		final RestRequest reqUncachedHeader = new RestRequest(Method.GET, "http://localhost/req");
		reqUncachedHeader.param("param", "value");
		reqUncachedHeader.header("Header", "text");
		reqUncachedHeader.header("Date", "123");
		match(req, reqUncachedHeader);

		final RestRequest reqMethod = new RestRequest(Method.POST, "http://localhost/req");
		reqMethod.param("param", "value");
		reqMethod.header("Header", "text");
		noMatch(req, reqMethod);

		final RestRequest reqMissingParam = new RestRequest(Method.GET, "http://localhost/req");
		reqMissingParam.header("Header", "text");
		noMatch(req, reqMissingParam);

		final RestRequest reqExtraParam = new RestRequest(Method.GET, "http://localhost/req");
		reqExtraParam.param("param", "value");
		reqExtraParam.param("param2", "value2");
		reqExtraParam.header("Header", "text");
		noMatch(req, reqExtraParam);

		final RestRequest reqMissingHeader = new RestRequest(Method.GET, "http://localhost/req");
		reqMissingHeader.param("param", "value");
		noMatch(req, reqMissingHeader);

		final RestRequest reqExtraHeader = new RestRequest(Method.GET, "http://localhost/req");
		reqExtraHeader.param("param", "value");
		reqExtraHeader.header("Header", "text");
		reqExtraHeader.header("Header2", "text2");
		noMatch(req, reqExtraHeader);

		final RestRequest reqAltUrl = new RestRequest(Method.GET, "http://localhost/alt");
		reqAltUrl.param("param", "value");
		reqAltUrl.header("Header", "text");
		noMatch(req, reqAltUrl);

		final RestRequest reqData = new RestRequest(Method.GET, "http://localhost/req");
		reqData.param("param", "value");
		reqData.header("Header", "text");
		reqData.data("testdata".getBytes());
		noMatch(req, reqData);

	}

	private void match(final RestRequest req1, final RestRequest req2) {
		assertEquals(req1, req2);
		assertEquals(req1.hashCode(), req2.hashCode());
	}

	private void noMatch(final RestRequest req1, final RestRequest req2) {
		assertNotEquals(req1, req2);
		assertNotEquals(req1.hashCode(), req2.hashCode());
	}

}
