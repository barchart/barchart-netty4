package com.barchart.netty.app;

import org.osgi.service.component.annotations.Reference;

import com.barchart.netty.api.NettyDotManager;

public class AppNetty extends AppConfig {

	//

	private NettyDotManager channelManager;

	protected NettyDotManager channelManager() {
		return channelManager;
	}

	@Reference
	protected void bind(final NettyDotManager s) {
		channelManager = s;
	}

	protected void unbind(final NettyDotManager s) {
		channelManager = null;
	}

}
