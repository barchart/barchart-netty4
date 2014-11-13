package com.barchart.netty.guice.http;

import io.netty.channel.ChannelHandler;

import com.barchart.netty.server.HandlerFactory;
import com.barchart.netty.server.http.request.RequestHandler;

public interface HttpService {

	/**
	 * Register a request handler factory for the specified URI path.
	 */
	public void registerHandler(String path, HandlerFactory<RequestHandler> handler);

	/**
	 * Register a web socket service handler at the specified URI path.
	 */
	public void registerWebSocket(final String path, final HandlerFactory<? extends ChannelHandler> factory);

	/**
	 * Unregister the handler at the specified URI path. If a handler was found,
	 * it will be returned.
	 */
	public void unregisterHandler(String path);

}
