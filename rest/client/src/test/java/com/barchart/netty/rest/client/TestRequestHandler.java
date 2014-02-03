package com.barchart.netty.rest.client;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;

import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.netty.server.http.request.RequestHandlerBase;
import com.barchart.util.test.concurrent.CallableTest;

public class TestRequestHandler extends RequestHandlerBase {

	public HttpHeaders headers = null;
	public HttpMethod method = null;
	public Map<String, List<String>> params = null;
	public byte[] input = null;

	public byte[] output = null;
	public HttpResponseStatus status = HttpResponseStatus.OK;

	@Override
	public void handle(final HttpServerRequest request) throws IOException {

		method = request.getMethod();
		params = request.getParameters();
		headers = request.headers();
		input = IOUtils.toByteArray(request.getInputStream());

		request.response().setStatus(status);
		request.response().write(output);
		request.response().finish();

	}

	public void reset() {

		headers = null;
		method = null;
		params = null;
		input = null;
		output = null;
		status = HttpResponseStatus.OK;

	}

	public void sync() throws Exception {
		CallableTest.waitFor(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return method != null;
			}
		}, 5000);
	}

}
