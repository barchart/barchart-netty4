/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.part.hand;

import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;
import io.netty.channel.ChannelPromise;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import com.barchart.netty.matrix.api.Matrix;

/** handler provides injection into matrix */
@Component(name = HandMatrix.TYPE, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class HandMatrix extends HandAny implements
		ChannelInboundMessageHandler<Object> {

	public static final String TYPE = "barchart.netty.hand.matrix";

	@Override
	public String type() {
		return TYPE;
	}

	private String sourceId;

	public void messageReceived(final ChannelHandlerContext ctx,
			final Object packet) throws Exception {

		matrix.process(sourceId, packet);

	}

	@Override
	public MessageBuf<Object> newInboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		return Unpooled.messageBuffer();
	}

	@Override
	public final void inboundBufferUpdated(final ChannelHandlerContext ctx)
			throws Exception {

		final MessageBuf<Object> source = ctx.inboundMessageBuffer();

		while (true) {
			final Object message = source.poll();
			if (message == null) {
				break;
			}
			messageReceived(ctx, message);
		}

	}

	private Matrix matrix;

	/** FIXME add bind filter */
	@Reference
	protected void bind(final Matrix s) {
		matrix = s;
	}

	protected void unbind(final Matrix s) {
		matrix = null;
	}

	@Override
	public void flush(final ChannelHandlerContext ctx,
			final ChannelPromise promise) throws Exception {
		// TODO Auto-generated method stub
		ctx.flush(promise);
	}

}
