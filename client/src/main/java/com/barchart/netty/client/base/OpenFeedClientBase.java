package com.barchart.netty.client.base;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;

import java.net.InetSocketAddress;

import com.barchart.account.api.Account;
import com.barchart.netty.client.transport.TransportProtocol;

public class OpenFeedClientBase<T extends OpenFeedClientBase<T>> extends
		AuthenticatingConnectableBase<T, Account> {

	protected OpenFeedClientBase(final EventLoopGroup eventLoop_,
			final InetSocketAddress address_, final TransportProtocol transport_) {

		super(eventLoop_, address_, transport_);

	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {

		// TODO Register codecs for messages based on OpenFeed header

	}

}
