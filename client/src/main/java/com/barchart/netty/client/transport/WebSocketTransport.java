package com.barchart.netty.client.transport;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.MessageBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelStateHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
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

public class WebSocketTransport implements TransportProtocol {

	private final URI uri;
	private final InetSocketAddress address;

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
				wsHandler);

		if (uri.getScheme().equalsIgnoreCase("wss")
				&& pipeline.get(SslHandler.class) == null) {

			final SSLEngine sslEngine =
					SSLContext.getDefault().createSSLEngine();
			sslEngine.setUseClientMode(true);
			pipeline.addFirst("ssl", new SslHandler(sslEngine));

		}

		// Fires CONNECTED event after handshake and removes self
		pipeline.addLast(new WebSocketConnectedNotifier());

		// BinaryWebSocketFrame <-> ByteBuf codec before user codecs
		pipeline.addLast(new WebSocketBinaryCodec());

	}

	private class WebSocketConnectedNotifier extends ChannelStateHandlerAdapter {

		@Override
		public void userEventTriggered(final ChannelHandlerContext ctx,
				final Object evt) throws Exception {

			if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
				ctx.fireUserEventTriggered(Event.CONNECTED);
				ctx.pipeline().remove(this);
			}

			ctx.fireUserEventTriggered(evt);

		}

		@Override
		public void inboundBufferUpdated(final ChannelHandlerContext ctx)
				throws Exception {
			ctx.fireInboundBufferUpdated();
		}

	}

	private class WebSocketBinaryCodec extends
			MessageToMessageCodec<BinaryWebSocketFrame, ByteBuf> {

		@Override
		protected void encode(final ChannelHandlerContext ctx,
				final ByteBuf msg, final MessageBuf<Object> out)
				throws Exception {
			out.add(new BinaryWebSocketFrame(msg));
		}

		@Override
		protected void decode(final ChannelHandlerContext ctx,
				final BinaryWebSocketFrame msg, final MessageBuf<Object> out)
				throws Exception {
			out.add(msg.content());
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

}
