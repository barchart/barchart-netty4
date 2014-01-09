/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;

import java.io.IOException;

import com.barchart.netty.server.http.HttpServerConfig;
import com.barchart.netty.server.http.error.ServerException;
import com.barchart.netty.server.http.error.ServerTooBusyException;
import com.barchart.netty.server.http.request.RequestHandler;
import com.barchart.netty.server.http.request.RequestHandlerMapping;

/**
 * Netty channel handler for routing inbound requests to the proper
 * RequestHandler.
 */
@Sharable
public class HttpRequestChannelHandler extends
		SimpleChannelInboundHandler<FullHttpRequest> {

	public static final AttributeKey<PooledServerResponse> ATTR_RESPONSE =
			AttributeKey.<PooledServerResponse> valueOf("response");

	private final HttpServerConfig config;
	private final ServerMessagePool messagePool;

	public HttpRequestChannelHandler(final HttpServerConfig config_) {
		super();
		config = config_;
		messagePool = new ServerMessagePool(config.maxConnections());
	}

	@Override
	public void channelRead0(final ChannelHandlerContext ctx,
			final FullHttpRequest msg) throws Exception {

		final RequestHandlerMapping mapping =
				config.getRequestMapping(msg.getUri());

		String relativePath = msg.getUri();

		if (mapping != null) {
			relativePath = relativePath.substring(mapping.path().length());
		}

		// Create request/response
		final PooledServerRequest request = messagePool.getRequest();

		// Handle 503 - sanity check, should be caught in acceptor
		if (request == null) {
			sendServerError(ctx, new ServerTooBusyException(
					"Maximum concurrent connections reached"));
			return;
		}

		request.init(ctx.channel(), msg, relativePath);

		final RequestHandler handler =
				mapping == null ? null : mapping.handler(request);

		final PooledServerResponse response = messagePool.getResponse();
		response.init(ctx, this, handler, request, config.logger());

		// Store in ChannelHandlerContext for future reference
		ctx.attr(ATTR_RESPONSE).set(response);

		try {

			if (handler == null) {
				response.setStatus(HttpResponseStatus.NOT_FOUND);
				config.errorHandler().onError(request, response, null);
			} else {
				handler.onRequest(request, response);
			}

		} catch (final Throwable t) {

			// Catch server errors
			response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);

			try {
				config.errorHandler().onError(request, response, t);
			} catch (final Throwable t2) {
				response.write(t.getClass()
						+ " was thrown while processing this request.  Additionally, "
						+ t2.getClass()
						+ " was thrown while handling this exception.");
			}

			config.logger().error(request, response, t);

			// Force request to end on exception, async handlers cannot allow
			// unchecked exceptions and still expect to return data
			try {
				if (!response.isFinished()) {
					response.finish();
				}
			} catch (final IOException ioe) {
				// Pretty likely at this point, swallow it because we don't care
			}

		}

	}

	private void sendServerError(final ChannelHandlerContext ctx,
			final ServerException cause) throws Exception {

		if (ctx.channel().isActive()) {

			final ByteBuf content = Unpooled.buffer();

			content.writeBytes((cause.getStatus().code() + " "
					+ cause.getStatus().reasonPhrase() + " - " + cause
					.getMessage()).getBytes());

			final FullHttpResponse response =
					new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
							cause.getStatus());

			response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,
					content.readableBytes());

			response.content().writeBytes(content);

			ctx.writeAndFlush(response)
					.addListener(ChannelFutureListener.CLOSE);

		}

	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx) {

		final PooledServerResponse response = ctx.attr(ATTR_RESPONSE).get();

		if (response != null) {

			try {

				if (!response.isFinished()) {

					response.close();

					final RequestHandler handler = response.handler();

					if (handler != null) {
						handler.onAbort(response.request(), response);
					}

				}

			} finally {

				freeHandlers(ctx);

			}

		}

	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx,
			final Throwable exception) throws Exception {

		final PooledServerResponse response = ctx.attr(ATTR_RESPONSE).get();

		if (response != null) {

			try {

				try {

					if (!response.isFinished()) {

						response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);

						config.errorHandler().onError(response.request(),
								response, exception);

						response.close();

						final RequestHandler handler = response.handler();

						if (handler != null) {
							handler.onException(response.request(), response,
									exception);
						}

					}

				} finally {

					config.logger().error(response.request(), response,
							exception);

				}

			} finally {

				freeHandlers(ctx);

			}

		}

	}

	/**
	 * Free any request/response handlers related to the current channel handler
	 * context.
	 */
	public void freeHandlers(final ChannelHandlerContext ctx) {

		final PooledServerResponse response =
				ctx.attr(ATTR_RESPONSE).getAndRemove();

		if (response != null) {

			try {

				final RequestHandler handler = response.handler();

				if (handler != null) {
					handler.onComplete(response.request(), response);
				}

			} finally {

				response.request().release();
				messagePool.makeAvailable(response.request());

				response.close();

				messagePool.makeAvailable(response);

			}

		}

	}

}
