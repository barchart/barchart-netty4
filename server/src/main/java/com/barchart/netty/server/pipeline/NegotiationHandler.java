/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import com.barchart.netty.common.messages.Capabilities;
import com.barchart.netty.common.messages.StartTLS;
import com.barchart.netty.common.messages.Version;
import com.barchart.netty.common.messages.VersionRequest;
import com.barchart.netty.common.messages.VersionResponse;
import com.barchart.netty.common.metadata.SecureAware;
import com.barchart.netty.common.metadata.VersionAware;

/**
 * Channel handler that negotiates initial connection properties with the client, including setting protocol versions
 * and channel encryption (TLS).
 */
public class NegotiationHandler extends ChannelInboundHandlerAdapter implements VersionAware, SecureAware {

	private final Version version;
	private final Version minVersion;
	private final Set<String> capabilities;

	private Version activeVersion;
	private boolean secure = false;

	private boolean cleanup = false;
	private ChannelHandler[] linked = null;

	public NegotiationHandler(final Version version_,
			final Version minVersion_, final String... capabilities_) {

		version = version_;
		minVersion = minVersion_;
		capabilities = new HashSet<String>(Arrays.asList(capabilities_));

	}

	/*
	 * Server can't broadcast before client sends messages due to websocket bug:
	 *
	 * https://github.com/netty/netty/issues/2173
	 */

	// @Override
	// public void channelActive(final ChannelHandlerContext ctx) throws
	// Exception {
	//
	// ctx.writeAndFlush(new Capabilities() {
	//
	// @Override
	// public Set<String> capabilities() {
	// return capabilities;
	// }
	//
	// @Override
	// public Version version() {
	// return version;
	// }
	//
	// @Override
	// public Version minVersion() {
	// return minVersion;
	// }
	//
	// });
	//
	// ctx.fireChannelActive();
	//
	// }

	@Override
	public void channelRead(final ChannelHandlerContext ctx,
			final Object msg) throws Exception {

		if (msg instanceof Capabilities) {

			ctx.writeAndFlush(new Capabilities() {

				@Override
				public Set<String> capabilities() {
					return capabilities;
				}

				@Override
				public Version version() {
					return version;
				}

				@Override
				public Version minVersion() {
					return minVersion;
				}

			});

		} else if (msg instanceof VersionRequest) {

			final VersionRequest request = (VersionRequest) msg;

			final Version v = request.version();

			if (minVersion.lessThanOrEqual(v) && version.greaterThanOrEqual(v)) {

				activeVersion = v;

				ctx.writeAndFlush(new VersionResponse() {

					@Override
					public boolean success() {
						return true;
					}

					@Override
					public Version version() {
						return v;
					}

				});

			} else {

				ctx.writeAndFlush(new VersionResponse() {

					@Override
					public boolean success() {
						return false;
					}

					@Override
					public Version version() {
						return version;
					}

				});

			}

		} else if (msg instanceof StartTLS) {

			// TODO Use a specific SSL cert?
			final SSLEngine sslEngine = SSLContext.getDefault().createSSLEngine();
			sslEngine.setUseClientMode(false);

			final SslHandler handler = new SslHandler(sslEngine, true);

			handler.handshakeFuture().addListener(
					new GenericFutureListener<Future<Channel>>() {

						@Override
						public void operationComplete(final Future<Channel> future)
								throws Exception {

							if (future.isSuccess()) {

								secure = true;

							} else {

								secure = false;

								// Failed, remove handler
								ctx.pipeline().remove(SslHandler.class);

							}

						}

					});

			// Add SslHandler to pipeline
			ctx.pipeline().addFirst(handler);

			// Confirm start TLS, initiate handshake
			ctx.writeAndFlush(new StartTLS() {
			});

		} else {

			ctx.fireChannelRead(msg);

			// First non-negotiation message, we're done - clean up pipeline
			if (cleanup) {

				ctx.pipeline().remove(this);

				if (linked != null) {
					for (final ChannelHandler handler : linked) {
						ctx.pipeline().remove(handler);
					}
				}

			}

		}

	}

	/**
	 * Remove self from pipeline after successful authentication, optionally removing linked handlers. time.
	 */
	public NegotiationHandler cleanup(final ChannelHandler... handlers_) {
		cleanup = true;
		linked = handlers_;
		return this;
	}

	@Override
	public Version version() {
		return activeVersion;
	}

	@Override
	public boolean secure() {
		return secure;
	}

}
