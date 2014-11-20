/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.rest.client;

import static org.junit.Assert.*;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.barchart.netty.rest.client.RestRequest.Method;
import com.barchart.netty.server.http.HttpServer;
import com.barchart.util.test.concurrent.TestObserver;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestRestClientBase {

	private static HttpServer server;
	private static RestClientBase client;
	private static TestRequestHandler handler;
	private static int port;

	@Test
	public void testBadURL() throws Exception {

		final RestClientBase client = new RestClientBase("http://xxx/yyy") {
		};

		handler.output = "out".getBytes("UTF-8");

		final RestRequest request = client.request(Method.GET, "/test");

		final TestObserver<RestResponse<byte[]>> observer =
				new TestObserver<RestResponse<byte[]>>();

		client.send(request).subscribe(observer);

		assertEquals(0, observer.sync().results.size());
		assertNotNull(observer.error);

	}

	@Test
	public void testBasicGet() throws Exception {

		handler.output = "out".getBytes("UTF-8");

		final RestRequest request = client.request(Method.GET, "/test");

		final TestObserver<RestResponse<byte[]>> observer =
				new TestObserver<RestResponse<byte[]>>();

		client.send(request).subscribe(observer);

		observer.sync();

		assertEquals(HttpResponseStatus.OK, handler.status);
		assertEquals(0, handler.input.length);
		assertEquals(1, observer.results.size());
		assertEquals(true, observer.results.get(0).success());
		assertEquals(200, observer.results.get(0).status());
		assertArrayEquals(handler.output, observer.results.get(0).content());

	}

	@Test
	public void testJsonPost() throws Exception {

		handler.output = "out".getBytes("UTF-8");

		final Map<String, Object> input = new HashMap<String, Object>();
		input.put("username", "user");

		final Map<String, Object> output = new HashMap<String, Object>();
		output.put("id", "1");

		final byte[] reqJson = new ObjectMapper().writeValueAsBytes(input);
		final byte[] acctJson = new ObjectMapper().writeValueAsBytes(output);

		handler.output = acctJson;

		final RestRequest request = client.request(Method.GET, "/test", input);

		final TestObserver<RestResponse<Map<String, Object>>> observer =
				new TestObserver<RestResponse<Map<String, Object>>>();

		client.send(request, new TypeReference<Map<String, Object>>() {
		}).subscribe(observer);

		observer.sync();

		assertEquals(HttpResponseStatus.OK, handler.status);
		assertArrayEquals(reqJson, handler.input);
		assertEquals(1, observer.results.size());
		assertEquals(true, observer.results.get(0).success());
		assertEquals(200, observer.results.get(0).status());
		assertEquals(output.get("id"),
				observer.results.get(0).content().get("id"));

	}

	@Before
	public void setUp() {
		handler.reset();
	}

	@BeforeClass
	public static void init() throws Exception {

		final ServerSocket s = new ServerSocket(0);
		port = s.getLocalPort();
		s.close();

		handler = new TestRequestHandler();

		server = new HttpServer().requestHandler("/test", handler);

		server.listen(port).sync();

		client = new RestClientBase("http://localhost:" + port) {
		};

	}

	@AfterClass
	public static void destroy() throws Exception {
		server.kill().sync();
	}

}
