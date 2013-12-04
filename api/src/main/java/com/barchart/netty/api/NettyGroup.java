package com.barchart.netty.api;

import io.netty.channel.EventLoopGroup;
import aQute.bnd.annotation.ProviderType;

/**
 * Represents netty thread pool.
 * <p>
 * FIXME change api.
 */
@ProviderType
public interface NettyGroup {

	/**
	 * Thread pool allocated as requested by {@link NettyDot} /
	 * {@link NettyHand} / {@link NettyPipe} policy.
	 */
	EventLoopGroup getGroup();

}
