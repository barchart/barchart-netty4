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
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;

import java.io.IOException;

import com.barchart.netty.server.HandlerFactory;
import com.barchart.netty.server.http.HttpServer;
import com.barchart.netty.server.http.error.ServerException;
import com.barchart.netty.server.http.error.ServerTooBusyException;
import com.barchart.netty.server.http.request.HandlerMapping;
import com.barchart.netty.server.http.request.RequestHandler;
import com.barchart.netty.server.http.websocket.WebSocketFramePacker;
import com.barchart.netty.server.http.websocket.WebSocketFrameUnpacker;

/**
 * Netty channel handler for routing inbound requests to the proper
 * RequestHandler.
 */
@Sharable
public class HttpRequestChannelHandler extends
		SimpleChannelInboundHandler<FullHttpRequest> implements KeepaliveHelper {

	public static final AttributeKey<PooledHttpServerRequest> ATTR_REQUEST =
			AttributeKey.<PooledHttpServerRequest> valueOf("request");

	private final HttpServer server;
	private final HttpServerRequestPool requestPool;

	public HttpRequestChannelHandler(final HttpServer server_) {
		super();
		server = server_;
		requestPool = new HttpServerRequestPool(server.maxConnections());
	}

	@Override
	public void channelRead0(final ChannelHandlerContext ctx,
			final FullHttpRequest msg) throws Exception {

		// Check if this path is a websocket
		final HandlerFactory<? extends ChannelHandler> factory =
				server.webSocketFactory(msg.getUri());

		if (factory != null) {
			startWebSocket(ctx, msg, factory);
			return;
		}

		// Find request handler
		final HandlerMapping<RequestHandler> mapping =
				server.getRequestMapping(msg.getUri());

		String relativePath = msg.getUri();

		if (mapping != null) {
			relativePath = relativePath.substring(mapping.path().length());
		}

		// Get an initialized request object from the pool
		final PooledHttpServerRequest request =
				requestPool.provision(ctx, msg, relativePath, this);

		// Handle 503 - sanity check, should be caught in acceptor
		if (request == null) {
			sendServerError(ctx, new ServerTooBusyException(
					"Maximum concurrent connections reached"));
			return;
		}

		final RequestHandler handler =
				mapping == null ? null : mapping.factory().newHandler();

		// Store in ChannelHandlerContext for future reference
		ctx.attr(ATTR_REQUEST).set(request);

		try {

			if (handler == null) {
				request.response().setStatus(HttpResponseStatus.NOT_FOUND);
				server.errorHandler().onError(request, null);
			} else {
				handler.handle(request);
			}

		} catch (final Throwable t) {

			// Catch server errors
			request.response().setStatus(
					HttpResponseStatus.INTERNAL_SERVER_ERROR);

			try {
				server.errorHandler().onError(request, t);
			} catch (final Throwable t2) {
				request.response()
						.write(t.getClass()
								+ " was thrown while processing this request.  Additionally, "
								+ t2.getClass()
								+ " was thrown while handling this exception.");
			}

			server.logger().error(request, t);

			// Force request to end on exception, async handlers cannot allow
			// unchecked exceptions and still expect to return data
			try {
				if (!request.response().isFinished()) {
					request.response().finish();
				}
			} catch (final IOException ioe) {
				// Pretty likely at this point, swallow it because we don't care
			}

		}

	}

	private void startWebSocket(final ChannelHandlerContext ctx,
			final FullHttpRequest msg,
			final HandlerFactory<? extends ChannelHandler> factory)
			throws Exception {

		// Websocket handshaker / decoder
		ctx.pipeline()
				.addLast(new WebSocketServerProtocolHandler(msg.getUri()));

		// Pack/unpack binary frames to ByteBuf
		ctx.pipeline().addLast(new WebSocketFramePacker(),
				new WebSocketFrameUnpacker());

		// Handlers should add any codecs they need to the pipeline using their
		// handlerAdded() method.
		ctx.pipeline().addLast(factory.newHandler());

		ctx.fireChannelRead(msg);

		// Remove self from pipeline, no need to process further HTTP messages
		ctx.pipeline().remove(this);

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

		requestComplete(ctx);

		ctx.fireChannelInactive();

	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx,
			final Throwable exception) throws Exception {

		final PooledHttpServerRequest request = ctx.attr(ATTR_REQUEST).get();

		if (request != null) {

			final PooledHttpServerResponse response = request.response();

			try {

				if (!response.isFinished()) {

					response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);

					server.errorHandler().onError(request, exception);

					// Bad handler, forgot to finish
					if (!response.isFinished()) {
						response.finish();
					}

				}

			} finally {

				server.logger().error(request, exception);

			}

		}

		ctx.fireExceptionCaught(exception);

	}

	/**
	 * Free any handlers related to the current request, record access and
	 * notify handler of completion.
	 */
	@Override
	public PooledHttpServerRequest requestComplete(
			final ChannelHandlerContext ctx) {

		final PooledHttpServerRequest request =
				ctx.attr(ATTR_REQUEST).getAndRemove();

		if (request != null) {

			// Record to access log
			server.logger().access(request,
					System.currentTimeMillis() - request.requestTime());

			try {

				try {

					if (!request.response().isFinished()) {

						request.response().close();

						final RequestHandler handler = request.handler();

						if (handler != null) {
							handler.cancel(request);
						}

					}

				} finally {

					final RequestHandler handler = request.handler();

					if (handler != null) {
						handler.release(request);
					}

				}

			} finally {

				requestPool.release(request);

			}

		}

		return request;

	}

}
