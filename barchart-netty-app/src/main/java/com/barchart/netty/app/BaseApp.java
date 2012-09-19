package com.barchart.netty.app;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Property;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.conf.event.ConfigEvent;
import com.barchart.conf.sync.api.ConfigManager;
import com.barchart.conf.util.ConfigAny;
import com.typesafe.config.Config;

@Component(enabled = true)
public class BaseApp implements EventHandler {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	/** provide event subscription topic filter */
	@Property(name = EventConstants.EVENT_TOPIC)
	static final String TOPIC = ConfigEvent.CONFIG_CHANGE;

	@Activate
	protected void activate() {

		log.info("config-activate");

		if (manager.isConfigValid()) {
			processChange("CONFIG VALID");
		}

	}

	@Deactivate
	protected void deactivate() {

		log.info("config-deactivate");

	}

	private ConfigManager manager;

	@Reference
	protected void bind(final ConfigManager s) {
		manager = s;
	}

	protected void unbind(final ConfigManager s) {
		manager = null;
	}

	@Override
	public void handleEvent(final Event event) {

		log.info("event-topic : {}", event.getTopic());

		if (manager.isConfigValid()) {
			processChange("CONFIG CHANGE");
		}

	}

	private Config config;

	protected Config config() {
		return config;
	}

	private void processChange(final String message) {

		log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

		log.info("\n{}", message);

		config = manager.getConfig();

		log.info("config : \n{}", ConfigAny.toString(config));

		log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

	}

}
