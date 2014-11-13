package com.barchart.netty.guice.http;

import static org.junit.Assert.assertEquals;
import io.netty.channel.ChannelPipeline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.util.guice.Component;
import com.barchart.util.guice.GuiceConfigBuilder;
import com.barchart.util.guice.StringResources;

public class TestHttpServiceProvider {

	private HttpServiceProvider service;
	private int port;

	@Before
	public void setUp() throws Exception {

		// Find an open port
		final ServerSocket ss = new ServerSocket(0);
		port = ss.getLocalPort();
		ss.close();

		// Wait a bit, Jenkins has issues here sometimes thinking the port is still bound
		Thread.sleep(100);

		final String config = "component = [\n"
				+ "{\n"
				+ "type = \"" + HttpServiceProvider.NAME + "\"\n"
				+ "local-address = \"0.0.0.0:" + port + "\"\n"
				+ "max-connections = -1\n"
				+ "}\n"
				+ "{\n"
				+ "type = \"http.handler\"\n"
				+ "}\n"
				+ "{\n"
				+ "type = \"websocket.handler\"\n"
				+ "}\n"
				+ "]";

		service = GuiceConfigBuilder.create()
				.setConfigResources(new StringResources(config)).build()
				.getInstance(HttpServiceProvider.class);

	}

	@After
	public void tearDown() throws Exception {
		service.deactivate();
	}

	@Test
	public void testRequest() throws Exception {

		final HttpClient client = new DefaultHttpClient();
		final HttpGet get = new HttpGet("http://localhost:" + port + "/test");
		final HttpResponse response = client.execute(get);

		assertEquals(1, TestHttpHandler.requests);

		final String content = new BufferedReader(new InputStreamReader(response.getEntity()
				.getContent())).readLine().trim();

		assertEquals("test", content);

	}

	@Component("http.handler")
	protected static class TestHttpHandler extends AbstractHttpRequestHandler {

		protected static volatile long requests = 0;

		@Override
		public void handle(final HttpServerRequest request) throws IOException {
			requests++;
			request.response().write("test");
			request.response().finish();
		}

		@Override
		public String path() {
			return "/test";
		}

	}

	@Component("websocket.handler")
	protected static class TestWebSocketHandler extends AbstractWebSocketRequestHandler {

		long requests = 0;

		@Override
		public String path() {
			return "/ws";
		}

		@Override
		public void initPipeline(final ChannelPipeline pipeline) throws Exception {
			// TODO Auto-generated method stub
		}

	}

}
