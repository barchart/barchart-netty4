package com.barchart.netty.server.http.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.File;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.URLConnection;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.cookie.DateUtils;
import org.junit.Before;
import org.junit.Test;

import com.barchart.netty.server.Servers;
import com.barchart.netty.server.http.HttpServer;

public class TestStaticResourceHandler {

	private HttpServer server;
	private HttpClient client;

	private int port;

	@Test
	public void testFileMimeType() throws Exception {

		final HttpGet get = new HttpGet("http://localhost:" + port + "/file/test.css");
		final HttpResponse response = client.execute(get);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("text/css", response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue());

	}

	@Test
	public void testChunked() throws Exception {

		final HttpGet get = new HttpGet("http://localhost:" + port + "/file/test.jpg");
		final HttpResponse response = client.execute(get);

		final File file = new File(System.getProperty("user.dir") + "/src/test/resources/files/test.jpg");

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals(null, response.getFirstHeader(HttpHeaders.CONTENT_LENGTH));
		assertTrue(IOUtils.contentEquals(
				new FileInputStream(file),
				response.getEntity().getContent()));
	}

	@Test
	public void testFileNotCached() throws Exception {

		final HttpGet get = new HttpGet("http://localhost:" + port + "/file/test.css");
		final HttpResponse response = client.execute(get);

		final File file = new File(System.getProperty("user.dir") + "/src/test/resources/files/test.css");

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals(file.length(), Integer.parseInt(response.getFirstHeader(HttpHeaders.CONTENT_LENGTH).getValue()));
		assertTrue(IOUtils.contentEquals(
				new FileInputStream(file),
				response.getEntity().getContent()));

	}

	@Test
	public void testFileCached() throws Exception {

		HttpGet get = new HttpGet("http://localhost:" + port + "/file/test.css");
		HttpResponse response = client.execute(get);

		final String modified = response.getFirstHeader(HttpHeaders.LAST_MODIFIED).getValue();

		get = new HttpGet("http://localhost:" + port + "/file/test.css");
		get.addHeader(HttpHeaders.IF_MODIFIED_SINCE, modified);
		response = client.execute(get);

		assertEquals(304, response.getStatusLine().getStatusCode());
		assertNull(response.getEntity());

	}

	@Test
	public void testFileCacheControl() throws Exception {

		HttpGet get = new HttpGet("http://localhost:" + port + "/file/test.css");
		HttpResponse response = client.execute(get);

		final String modified = response.getFirstHeader(HttpHeaders.LAST_MODIFIED).getValue();

		get = new HttpGet("http://localhost:" + port + "/file/test.css");
		get.addHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		get.addHeader(HttpHeaders.IF_MODIFIED_SINCE, modified);
		response = client.execute(get);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertTrue(IOUtils.contentEquals(
				new FileInputStream(new File(System.getProperty("user.dir") + "/src/test/resources/files/test.css")),
				response.getEntity().getContent()));

	}

	@Test
	public void testFileCacheExpired() throws Exception {

		HttpGet get = new HttpGet("http://localhost:" + port + "/file/test.css");
		HttpResponse response = client.execute(get);

		final String modified = response.getFirstHeader(HttpHeaders.LAST_MODIFIED).getValue();
		final long modtime = DateUtils.parseDate(modified).getTime();

		get = new HttpGet("http://localhost:" + port + "/file/test.css");
		get.addHeader(HttpHeaders.IF_MODIFIED_SINCE, DateUtils.formatDate(new Date(modtime - 1000)));
		response = client.execute(get);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertTrue(IOUtils.contentEquals(
				new FileInputStream(new File(System.getProperty("user.dir") + "/src/test/resources/files/test.css")),
				response.getEntity().getContent()));

	}

	@Test
	public void testClasspathMimeType() throws Exception {

		final HttpGet get = new HttpGet("http://localhost:" + port + "/classpath/test.css");
		final HttpResponse response = client.execute(get);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("text/css", response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue());

	}

	@Test
	public void testClasspathNotCached() throws Exception {

		final HttpGet get = new HttpGet("http://localhost:" + port + "/classpath/test.css");
		final HttpResponse response = client.execute(get);

		final URLConnection conn = getClass().getResource("/files/test.css").openConnection();

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals(conn.getContentLength(),
				Integer.parseInt(response.getFirstHeader(HttpHeaders.CONTENT_LENGTH).getValue()));
		assertTrue(IOUtils.contentEquals(
				conn.getInputStream(),
				response.getEntity().getContent()));

	}

	@Test
	public void testClasspathCached() throws Exception {

		HttpGet get = new HttpGet("http://localhost:" + port + "/classpath/test.css");
		HttpResponse response = client.execute(get);

		final String modified = response.getFirstHeader(HttpHeaders.LAST_MODIFIED).getValue();

		get = new HttpGet("http://localhost:" + port + "/classpath/test.css");
		get.addHeader(HttpHeaders.IF_MODIFIED_SINCE, modified);
		response = client.execute(get);

		assertEquals(304, response.getStatusLine().getStatusCode());
		assertNull(response.getEntity());

	}

	@Test
	public void testClasspathCacheControl() throws Exception {

		HttpGet get = new HttpGet("http://localhost:" + port + "/classpath/test.css");
		HttpResponse response = client.execute(get);

		final String modified = response.getFirstHeader(HttpHeaders.LAST_MODIFIED).getValue();

		get = new HttpGet("http://localhost:" + port + "/classpath/test.css");
		get.addHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		get.addHeader(HttpHeaders.IF_MODIFIED_SINCE, modified);
		response = client.execute(get);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertTrue(IOUtils.contentEquals(
				getClass().getResourceAsStream("/files/test.css"),
				response.getEntity().getContent()));

	}

	@Test
	public void testClasspathCacheExpired() throws Exception {

		HttpGet get = new HttpGet("http://localhost:" + port + "/classpath/test.css");
		HttpResponse response = client.execute(get);

		final String modified = response.getFirstHeader(HttpHeaders.LAST_MODIFIED).getValue();
		final long modtime = DateUtils.parseDate(modified).getTime();

		get = new HttpGet("http://localhost:" + port + "/classpath/test.css");
		get.addHeader(HttpHeaders.IF_MODIFIED_SINCE, DateUtils.formatDate(new Date(modtime - 1000)));
		response = client.execute(get);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertTrue(IOUtils.contentEquals(
				getClass().getResourceAsStream("/files/test.css"),
				response.getEntity().getContent()));

	}

	@Before
	public void setUp() throws Exception {

		final ServerSocket s = new ServerSocket(0);
		port = s.getLocalPort();
		s.close();

		server =
				Servers.createHttpServer()
						.group(new NioEventLoopGroup())
						.requestHandler("/classpath",
								new StaticResourceHandler(this.getClass().getClassLoader(), "/files"))
						.requestHandler("/file",
								new StaticResourceHandler(new File(System.getProperty("user.dir")
										+ "/src/test/resources/files")));

		server.listen(port, "localhost");

		client = new DefaultHttpClient(new PoolingClientConnectionManager());

	}

}
