package com.barchart.netty.part.hand;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelStateHandlerAdapter;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

@Sharable
public abstract class ChannelInitializerXXX<C extends Channel> extends
		ChannelStateHandlerAdapter {

	private static final InternalLogger logger = InternalLoggerFactory
			.getInstance(ChannelInitializerXXX.class);

	public abstract void initChannel(C ch) throws Exception;

	@Override
	public final void channelRegistered(final ChannelHandlerContext ctx)
			throws Exception {
		boolean removed = false;
		boolean success = false;
		try {
			initChannel((C) ctx.channel());
			ctx.pipeline().remove(this);
			removed = true;
			ctx.fireChannelRegistered();
			success = true;
		} catch (final Throwable t) {
			logger.warn(
					"Failed to initialize a channel. Closing: " + ctx.channel(),
					t);
		} finally {
			if (!removed) {
				ctx.pipeline().remove(this);
			}
			if (!success) {
				ctx.close();
			}
		}
	}
}
