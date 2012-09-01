package com.barchart.netty.host.impl;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.host.api.NettyDotManager;
import com.barchart.netty.host.api.NettyGroup;
import com.barchart.netty.util.point.NetPoint;
import com.barchart.osgi.factory.api.FidgetManagerBase;
import com.typesafe.config.Config;

/**
 * dot factory manager
 * 
 * FIXME get thread pool params from config admin
 */
@Component(immediate = true)
public class NettyDotProvider extends FidgetManagerBase<NettyDot> implements
		NettyDotManager, NettyGroup {

	static {
		new NettySetup();
	}

	protected final Logger log = LoggerFactory.getLogger(getClass());

	//

	private final Timer timer = new HashedWheelTimer();

	public Timer getTimer() {
		return timer;
	}

	//

	@Override
	@Activate
	protected void activate(final ComponentContext c) {

		log.debug("@@@ ACTIVE");

		// threadPool = threadPoolManager.get(POOL_NAME);

		group = new NioEventLoopGroup(10, threadFactory);

		super.activate(c);

	}

	@Override
	@Deactivate
	protected void deactivate(final ComponentContext c) {

		log.debug("@@@ INACTIVE");

		// threadPoolManager.release(threadPool);

		group.shutdown();

		super.deactivate(c);

	}

	@Override
	protected Class<NettyDot> getFidgetInterface() {
		return NettyDot.class;
	}

	// private static final String POOL_NAME = NettyManagerProvider.class
	// .getName();
	// private ThreadPool threadPool;
	// private ThreadPoolManager threadPoolManager;
	// @Reference
	// protected void bind(final ThreadPoolManager s) {
	// threadPoolManager = s;
	// }
	// protected void unbind(final ThreadPoolManager s) {
	// threadPoolManager = null;
	// }

	private final ThreadFactory threadFactory = new NettyThreadFactory();

	private EventLoopGroup group;

	@Override
	public EventLoopGroup getGroup() {
		return group;
	}

	@Override
	public NettyDot create(final Config config) {

		final String type = config.getString(NetPoint.KEY_TYPE);

		final String hocon = config.root().render();

		final Map<String, String> props = new HashMap<String, String>();

		props.put(NettyDot.PROP_NET_POINT, hocon);

		return create(type, props);

	}

}
