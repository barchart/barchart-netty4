/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.barchart.netty.part.hand;

import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;
import io.netty.channel.socket.DatagramPacket;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.barchart.netty.matrix.api.Matrix;

/** handler provide injection into matix */
@Component(factory = HandMatrix.FACTORY)
public class HandMatrix extends HandAny implements
		ChannelInboundMessageHandler<DatagramPacket> {

	public static final String FACTORY = "barchart.netty.hand.matrix";

	@Override
	public String getFactoryId() {
		return FACTORY;
	}

	private final String sourceId;

	public HandMatrix(final String sourceId, final Matrix matrix) {

		this.sourceId = sourceId;

		this.matrix = matrix;

	}

	public void messageReceived(final ChannelHandlerContext ctx,
			final DatagramPacket packet) throws Exception {

		matrix.process(sourceId, packet);

	}

	@Override
	public MessageBuf<DatagramPacket> newInboundBuffer(
			final ChannelHandlerContext ctx) throws Exception {
		return Unpooled.messageBuffer();
	}

	@Override
	public final void inboundBufferUpdated(final ChannelHandlerContext ctx)
			throws Exception {
		final MessageBuf<DatagramPacket> in = ctx.inboundMessageBuffer();
		for (;;) {
			final DatagramPacket msg = in.poll();
			if (msg == null) {
				break;
			}
			try {
				messageReceived(ctx, msg);
			} catch (final Throwable t) {
				ctx.fireExceptionCaught(t);
			}
		}
	}

	private Matrix matrix;

	@Reference
	protected void bind(final Matrix s) {
		matrix = s;
	}

	protected void unbind(final Matrix s) {
		matrix = null;
	}

}
