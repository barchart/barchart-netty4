package com.barchart.rest.client.transport;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.Before;
import org.junit.Test;

import com.barchart.netty.server.Servers;
import com.barchart.netty.server.http.HttpServer;
import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.netty.server.http.request.RequestHandlerBase;
import com.barchart.rest.client.RestRequest;
import com.barchart.rest.client.RestRequest.Method;
import com.barchart.rest.client.RestResponse;
import com.barchart.util.test.concurrent.TestObserver;

public class TestURLConnectionTransport {

	private int port;

	@Test
	public void testGet() throws Exception {

		final URLConnectionTransport transport = new URLConnectionTransport();

		final RestRequest<Void> request =
				new RestRequest<Void>(Method.GET, "http://localhost:" + port
						+ "/test");

		final TestObserver<RestResponse<byte[]>> observer =
				new TestObserver<RestResponse<byte[]>>();

		transport.send(request).subscribe(observer);

		assertEquals(1, observer.sync().results.size());
		assertEquals(400, observer.results.get(0).status());
		assertEquals(false, observer.results.get(0).success());
		assertEquals("GET", observer.results.get(0).error());
		assertArrayEquals("GET".getBytes("UTF-8"), observer.results.get(0)
				.content());

	}

	@Test
	public void testPost() throws Exception {

		final URLConnectionTransport transport = new URLConnectionTransport();

		final RestRequest<Void> request =
				new RestRequest<Void>(Method.POST, "http://localhost:" + port
						+ "/test");
		request.data("testing".getBytes("UTF-8"));

		final TestObserver<RestResponse<byte[]>> observer =
				new TestObserver<RestResponse<byte[]>>();

		transport.send(request).subscribe(observer);

		assertEquals(1, observer.sync().results.size());
		assertEquals(200, observer.results.get(0).status());
		assertEquals(true, observer.results.get(0).success());
		assertNull(observer.results.get(0).error());
		assertArrayEquals("POST".getBytes("UTF-8"), observer.results.get(0)
				.content());

	}

	@Before
	public void setUp() throws Exception {

		final ServerSocket s = new ServerSocket(0);
		port = s.getLocalPort();
		s.close();

		final HttpServer server =
				Servers.createHttpServer().requestHandler("/test",
						new RequestHandlerBase() {
							@Override
							public void handle(final HttpServerRequest request)
									throws IOException {

								if (request.getMethod() == HttpMethod.POST) {
									request.response().setStatus(
											HttpResponseStatus.OK);
								} else {
									request.response().setStatus(
											HttpResponseStatus.BAD_REQUEST);
								}
								request.response().write(
										request.getMethod().name());
								request.response().finish();

							}
						});

		server.listen(port, "localhost").sync();

	}
}
