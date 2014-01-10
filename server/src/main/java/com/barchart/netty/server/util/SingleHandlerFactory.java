package com.barchart.netty.server.util;

import com.barchart.netty.server.HandlerFactory;


public class SingleHandlerFactory<H> implements HandlerFactory<H> {

	private final H handler;

	public SingleHandlerFactory(final H handler_) {
		handler = handler_;
	}

	@Override
	public H newHandler() {
		return handler;
	}

}