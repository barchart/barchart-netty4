package com.barchart.netty.rest.client.cache;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import rx.Observable;

import com.barchart.netty.rest.client.RestEndpoint;
import com.barchart.netty.rest.client.RestRequest;
import com.barchart.netty.rest.client.RestRequest.Method;
import com.barchart.netty.rest.client.RestResponse;

public class TestDefaultRestResponseCache {

	private DefaultRestResponseCache cache;

	@Before
	public void setUp() throws Exception {
		cache = new DefaultRestResponseCache();
		cache.cache("/test", 10, 200, TimeUnit.MILLISECONDS);
		cache.cache("/test/more", 10, 200, TimeUnit.MILLISECONDS);
	}

	@Test
	public void testPassThrough() throws Exception {
		cache.intercept(request("/passthrough"), Observable.just(success("passthrough"))).toBlockingObservable().last();
		assertEquals(2, cache.responses.size());
		assertEquals(0, cache.responses.get(new RestEndpoint("/test")).size());
		assertEquals(0, cache.responses.get(new RestEndpoint("/test/more")).size());
	}

	@Test
	public void testSuccessCache() throws Exception {
		final RestRequest request = request("/test");
		final RestResponse<String> response = success("testSuccessCache");
		cache.intercept(request, Observable.just(response)).toBlockingObservable().last();
		assertEquals(1, cache.responses.get(new RestEndpoint("/test")).size());
		assertEquals(response, cache.responses.get(new RestEndpoint("/test")).getIfPresent(request));
	}

	@Test
	public void testCachePriority() throws Exception {
		final RestRequest request = request("/test/more");
		final RestResponse<String> response = success("testCachePriority");
		cache.intercept(request, Observable.just(response)).toBlockingObservable().last();
		assertEquals(0, cache.responses.get(new RestEndpoint("/test")).size());
		assertEquals(1, cache.responses.get(new RestEndpoint("/test/more")).size());
		assertEquals(response, cache.responses.get(new RestEndpoint("/test/more")).getIfPresent(request));
	}

	@Test
	public void testImpliedFailureCache() throws Exception {
		final RestRequest request = request("/test");
		final RestResponse<String> response = failure("testImpliedFailureCache");
		cache.intercept(request, Observable.just(response)).toBlockingObservable().last();
		assertEquals(1, cache.responses.get(new RestEndpoint("/test")).size());
		assertEquals(response, cache.responses.get(new RestEndpoint("/test")).getIfPresent(request));
	}

	@Test
	public void testFailureCache() throws Exception {
		cache.failure("/test", 10, 5000, TimeUnit.MILLISECONDS);
		final RestRequest request = request("/test");
		final RestResponse<String> response = failure("testFailureCache");
		cache.intercept(request, Observable.just(response)).toBlockingObservable().last();
		assertEquals(0, cache.responses.get(new RestEndpoint("/test")).size());
		assertEquals(1, cache.failures.get(new RestEndpoint("/test")).size());
		assertEquals(response, cache.failures.get(new RestEndpoint("/test")).getIfPresent(request));
	}

	private RestRequest request(final String path) {
		return new RestRequest(Method.GET, "http://localhost" + path);
	}

	private RestResponse<String> success(final String value) {

		return new RestResponse<String>() {

			@Override
			public boolean success() {
				return true;
			}

			@Override
			public int status() {
				return 200;
			}

			@Override
			public String error() {
				return null;
			}

			@Override
			public Map<String, List<String>> headers() {
				return null;
			}

			@Override
			public String content() {
				return value;
			}

		};

	}

	private RestResponse<String> failure(final String value) {

		return new RestResponse<String>() {

			@Override
			public boolean success() {
				return false;
			}

			@Override
			public int status() {
				return 500;
			}

			@Override
			public String error() {
				return value;
			}

			@Override
			public Map<String, List<String>> headers() {
				return null;
			}

			@Override
			public String content() {
				return null;
			}

		};

	}

}
