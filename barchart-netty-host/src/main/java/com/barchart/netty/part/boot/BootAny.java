package com.barchart.netty.part.boot;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;

import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.host.api.NettyBoot;
import com.barchart.netty.host.api.NettyGroup;
import com.barchart.netty.host.api.NettyPipe;
import com.barchart.netty.host.api.NettyPipeManager;
import com.barchart.netty.util.point.NetPoint;

public abstract class BootAny implements NettyBoot {

	protected static final Logger log = LoggerFactory.getLogger(BootAny.class);

	@Override
	public ChannelFuture shutdown(final NetPoint netPoint, final Channel channel)
			throws Exception {
		return channel.close();
	}

	/**
	 * builder for transient pipeline applicator handler
	 */
	protected final ChannelInitializer<Channel> pipeApply(
			final NetPoint netPoint, final NettyPipe.Mode mode) {

		return new ChannelInitializer<Channel>() {
			@Override
			public void initChannel(final Channel channel) throws Exception {

				final NettyPipe pipe =
						pipeManager().findPipe(netPoint.getPipeline());

				if (pipe == null) {
					log.error("missing pipeline", new IllegalArgumentException(
							netPoint.getPipeline()));
				} else {
					// FIXME: nulled since we aren't required to use Dots here
					pipe.apply(null, channel, mode);
				}

			}
		};

	}

	private EventLoopGroup group;

	protected EventLoopGroup group() {
		return group;
	}

	@Reference
	protected void bind(final NettyGroup s) {
		group = s.getGroup();
	}

	protected void unbind(final NettyGroup s) {
		group = null;
	}

	private NettyPipeManager pipeManager;

	protected NettyPipeManager pipeManager() {
		return pipeManager;
	}

	@Reference
	protected void bind(final NettyPipeManager pm) {
		pipeManager = pm;
	}

	protected void unbind(final NettyPipeManager pm) {
		pipeManager = null;
	}

}
