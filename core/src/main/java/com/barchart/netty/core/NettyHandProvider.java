package com.barchart.netty.core;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.api.NettyHand;
import com.barchart.netty.api.NettyHandManager;
import com.barchart.osgi.factory.api.CidgetManagerBase;

/**
 * handler factory manager
 */
@Component(immediate = true)
public class NettyHandProvider extends CidgetManagerBase<NettyHand> implements
		NettyHandManager {

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

		super.activate(c);

		log.debug("@@@ ACTIVE");

	}

	@Override
	@Deactivate
	protected void deactivate(final ComponentContext c) {

		log.debug("@@@ INACTIVE");

		super.deactivate(c);

	}

	@Override
	protected Class<NettyHand> interfaceClass() {
		return NettyHand.class;
	}

}
