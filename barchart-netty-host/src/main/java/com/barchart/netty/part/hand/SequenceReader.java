package com.barchart.netty.part.hand;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class SequenceReader extends ChannelHandlerAdapter {

	private ChannelHandlerContext ctx;

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {

		this.ctx = ctx;

	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx)
			throws Exception {

		this.ctx = null;

	}

}
