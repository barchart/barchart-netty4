package com.barchart.netty.client.base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelStateHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;

import com.barchart.netty.client.facets.SecureAware;
import com.barchart.netty.client.pipeline.SecureFlowHandler;
import com.barchart.netty.client.transport.TransportProtocol;

public abstract class SecureConnectableBase<T extends SecureConnectableBase<T>>
		extends ConnectableBase<T> implements SecureAware {

	protected abstract static class Builder<B extends Builder<B, C>, C extends SecureConnectableBase<C>>
			extends ConnectableBase.Builder<B, C> {

		protected SecureAware.Request security;

		@SuppressWarnings("unchecked")
		public B secure(final SecureAware.Request security_) {
			security = security_;
			return (B) this;
		}

		@Override
		protected C configure(final C client) {
			super.configure(client);
			client.security(security);
			return client;
		}
	}

	private SecureAware.Request security = SecureAware.Request.OPTIONAL;

	private boolean secure = false;

	protected SecureConnectableBase(final EventLoopGroup eventLoop_,
			final TransportProtocol transport_) {

		super(eventLoop_, transport_);

	}

	protected void security(final SecureAware.Request security_) {
		security = security_;
	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {

		switch (security) {

			case REFUSE:
				// No security
				break;

			case REQUIRE:
				pipeline.addLast(new SecureFlowHandler(true));
				pipeline.addLast(new SslHandshakeListener());
				break;

			case OPTIONAL:
			default:
				pipeline.addLast(new SecureFlowHandler(false));
				pipeline.addLast(new SslHandshakeListener());

		}

	}

	@Override
	public boolean secure() {
		return secure;
	}

	private class SslHandshakeListener extends ChannelStateHandlerAdapter {

		@Override
		public void userEventTriggered(final ChannelHandlerContext ctx,
				final Object evt) throws Exception {

			if (evt == SslHandshakeCompletionEvent.SUCCESS) {
				secure = true;
				ctx.pipeline().remove(this);
			}

		}

		@Override
		public void inboundBufferUpdated(final ChannelHandlerContext ctx)
				throws Exception {
			ctx.fireInboundBufferUpdated();
		}

	}

}
