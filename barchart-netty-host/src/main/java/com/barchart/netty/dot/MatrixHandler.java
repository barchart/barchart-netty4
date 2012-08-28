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
package com.barchart.netty.dot;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

import com.barchart.netty.matrix.api.Matrix;

public class MatrixHandler extends
		ChannelInboundMessageHandlerAdapter<DatagramPacket> {

	private final String sourceId;

	private final Matrix matrix;

	public MatrixHandler(final String sourceId, final Matrix matrix) {

		this.sourceId = sourceId;

		this.matrix = matrix;

	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
			final DatagramPacket packet) throws Exception {

		matrix.process(sourceId, packet);

	}

}
