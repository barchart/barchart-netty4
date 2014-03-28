/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.client.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import com.barchart.netty.common.pipeline.WebSocketBinaryCodec;
import com.barchart.netty.common.pipeline.WebSocketConnectedNotifier;

/**
 * Websocket streaming transport. Automatically upgrades the HTTP connection to
 * websockets and handles encoding/decoding bytes into WebSocket frames so that
 * to downstream ChannelHandlers it just looks like a standard byte stream.
 */
public class WebSocketTransport implements TransportProtocol {

	private final URI uri;
	private final InetSocketAddress address;

	/**
	 * Construct a WebSocketTransport with the specified URI. If the
	 * <code>wss://</code> protocol is specified, SSL-TLS will be activated for
	 * the connection.
	 */
	public WebSocketTransport(final URI uri_) {

		uri = uri_;

		int port = uri.getPort();

		if (port == -1) {
			port = uri.getScheme().equalsIgnoreCase("wss") ? 443 : 80;
		}

		address = new InetSocketAddress(uri.getHost(), port);

	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {

		final WebSocketClientHandshaker handshaker =
				WebSocketClientHandshakerFactory.newHandshaker(uri,
						WebSocketVersion.V13, null, false, null);

		final WebSocketClientProtocolHandler wsHandler =
				new WebSocketClientProtocolHandler(handshaker);

		pipeline.addFirst(new HttpClientCodec(), //
				new HttpObjectAggregator(65536), //
				wsHandler,
				// Fires channelActive() after handshake and removes self
				new WebSocketConnectedNotifier(),
				// BinaryWebSocketFrame <-> ByteBuf codec before user codecs
				new WebSocketBinaryCodec());

		if (uri.getScheme().equalsIgnoreCase("wss")
				&& pipeline.get(SslHandler.class) == null) {

			final SSLEngine sslEngine =
					SSLContext.getDefault().createSSLEngine();
			sslEngine.setUseClientMode(true);
			pipeline.addFirst("ssl", new SslHandler(sslEngine));

		}

	}

	@Override
	public Class<? extends Channel> channel() {
		return NioSocketChannel.class;
	}

	@Override
	public SocketAddress address() {
		return address;
	}

	@Override
	public Bootstrap bootstrap() {
		return DEFAULT_BOOTSTRAP.clone().channel(channel())
				.remoteAddress(address());
	}

}
