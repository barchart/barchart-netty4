package com.barchart.netty.server;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;

/**
 * Base class for building a Netty server.
 * 
 * @param <S> The server type
 * @param <B> This builder type
 */
public interface ServerBuilder<S extends Server<S>, B extends ServerBuilder<S, B>> {

	/**
	 * Set the socket channel type.
	 */
	B channel(final Class<? extends ServerChannel> type);

	/**
	 * Set the parent (listen port) event loop group.
	 */
	B parentGroup(final EventLoopGroup group);

	/**
	 * Set the child (request handler) event loop group.
	 */
	B childGroup(final EventLoopGroup group);

	/**
	 * Build the server from current configuration.
	 */
	S build();

}
