package com.barchart.netty.server;

import io.netty.channel.EventLoopGroup;

import java.net.SocketAddress;

/**
 * Base class for building a Netty server.
 * 
 * @param <S> The server type
 * @param <B> This builder type
 */
public interface ServerBuilder<S extends NettyServer, B extends ServerBuilder<S, B>> {

	/**
	 * Set the maximum number of client connections.
	 */
	B maxConnections(final int max);

	/**
	 * Set the parent (listen port) event loop group.
	 */
	B parentGroup(final EventLoopGroup group);

	/**
	 * Set the child (request handler) event loop group.
	 */
	B childGroup(final EventLoopGroup group);

	/**
	 * Start a server on the specified port.
	 */
	S listen(final int port);

	/**
	 * Start a server on the specified port and hostname (or IP).
	 */
	S listen(final int port, final String hostOrIp);

	/**
	 * Start a server on the specified address.
	 */
	S listen(final SocketAddress address);

}
