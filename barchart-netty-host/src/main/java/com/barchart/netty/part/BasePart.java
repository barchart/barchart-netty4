package com.barchart.netty.part;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasePart {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	//

	private volatile boolean isActive;

	public boolean isActive() {
		return isActive;
	}

	//

	@Activate
	protected void activate() {

		if (isActive) {
			log.error("", new Exception("already active"));
		} else {
			log.debug("activate");
		}

		isActive = true;

	}

	@Deactivate
	protected void deactivate() {

		if (!isActive) {
			log.error("", new Exception("was not active"));
		} else {
			log.debug("deactivate");
		}

		isActive = false;

	}

}
