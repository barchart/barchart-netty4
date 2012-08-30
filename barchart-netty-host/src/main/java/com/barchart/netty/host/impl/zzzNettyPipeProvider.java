package com.barchart.netty.host.impl;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.host.api.zzzNettyPipe;
import com.barchart.netty.host.api.zzzNettyPipeManager;
import com.barchart.osgi.factory.api.FidgetManagerBase;

/**
 * pipeline factory manager
 */
// @Component(immediate = true)
public class zzzNettyPipeProvider extends FidgetManagerBase<zzzNettyPipe>
		implements zzzNettyPipeManager {

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

		super.activate(c);

	}

	@Override
	@Deactivate
	protected void deactivate(final ComponentContext c) {

		log.debug("@@@ INACTIVE");

		super.deactivate(c);

	}

	@Override
	protected Class<zzzNettyPipe> getFidgetInterface() {
		return zzzNettyPipe.class;
	}

}
