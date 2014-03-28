/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.rest.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.barchart.netty.rest.server.RestHandlerBase;
import com.barchart.netty.server.http.request.HttpServerRequest;

public class TestHandler extends RestHandlerBase {

	private final String name;

	public int requests = 0;
	public int get = 0;
	public int post = 0;
	public int put = 0;
	public int delete = 0;
	public int exceptions = 0;
	public String user = null;
	public Map<String, List<String>> params;

	public TestHandler(final String name_) {
		name = name_;
	}

	@Override
	public void get(final HttpServerRequest request) throws IOException {
		get++;
	}

	@Override
	public void put(final HttpServerRequest request) throws IOException {
		put++;
	}

	@Override
	public void post(final HttpServerRequest request) throws IOException {
		post++;
	}

	@Override
	public void delete(final HttpServerRequest request) throws IOException {
		delete++;
	}

	@Override
	public void handle(final HttpServerRequest request) throws IOException {
		requests++;
		user = request.getRemoteUser();
		params = request.getParameters();
		super.handle(request);
	}

	@Override
	public void cancel(final HttpServerRequest request) {
	}

	@Override
	public void release(final HttpServerRequest request) {
	}

}