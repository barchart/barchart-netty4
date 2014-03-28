/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import com.barchart.netty.common.messages.StartTLS;
import com.barchart.netty.common.metadata.SecureAware;

public class StartTLSHandler extends SimpleChannelInboundHandler<StartTLS>
		implements SecureAware {

	private boolean secure = false;

	@Override
	protected void channelRead0(final ChannelHandlerContext ctx,
			final StartTLS msg) throws Exception {

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

	}

	@Override
	public boolean secure() {
		return secure;
	}

}
