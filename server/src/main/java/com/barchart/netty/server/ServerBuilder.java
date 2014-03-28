/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.EventLoopGroup;

import com.barchart.netty.common.pipeline.PipelineInitializer;
import com.barchart.netty.server.base.BootstrapInitializer;

/**
 * Base class for building a Netty server.
 * 
 * @param <S> The server type
 * @param <B> This builder type
 */
public interface ServerBuilder<S extends Server<S>, T extends AbstractBootstrap<T, ?>, B extends ServerBuilder<S, T, B>> {

	/**
	 * Set the parent (listen port) event loop group.
	 */
	B group(final EventLoopGroup group);

	/**
	 * Pipeline initializer for new channels.
	 */
	B pipeline(final PipelineInitializer inititalizer);

	/**
	 * Bootstrap initializer for the acceptor channel.
	 */
	B bootstrapper(final BootstrapInitializer<T> inititalizer);

	/**
	 * Build the server from current configuration.
	 */
	S build();

}
