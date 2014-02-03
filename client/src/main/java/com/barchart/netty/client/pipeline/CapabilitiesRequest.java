package com.barchart.netty.client.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Collections;
import java.util.Set;

import com.barchart.netty.common.messages.Capabilities;
import com.barchart.netty.common.messages.Version;

/**
 * Channel handler that immediately writes a Capabilities message to the server
 * on connect, and expects a Capabilities message response.
 *
 * This handler removes itself after sending the request.
 */
public class CapabilitiesRequest extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws
			Exception {

		ctx.writeAndFlush(new Capabilities() {

			@Override
			public Set<String> capabilities() {
				return Collections.emptySet();
			}

			@Override
			public Version version() {
				return null;
			}

			@Override
			public Version minVersion() {
				return null;
			}

		});

		ctx.fireChannelActive();

		ctx.pipeline().remove(this);

	}

}
