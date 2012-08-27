package com.barchart.netty.part;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasePart {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	//

	private volatile boolean isRunning;

	public boolean isRunning() {
		return isRunning;
	}

	//

	@Activate
	protected void start() {

		if (isRunning) {
			log.error("", new Exception("already running"));
		}

		isRunning = true;

	}

	@Deactivate
	protected void stop() {

		if (!isRunning) {
			log.error("", new Exception("was not running"));
		}

		isRunning = false;

	}

}
