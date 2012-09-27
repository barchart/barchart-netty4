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

}
