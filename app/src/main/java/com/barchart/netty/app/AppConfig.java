package com.barchart.netty.app;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
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
import com.typesafe.config.ConfigFactory;

public class AppConfig implements EventHandler {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	/** provide event subscription topic filter */
	@Property(name = EventConstants.EVENT_TOPIC)
	static final String TOPIC = ConfigEvent.CONFIG_CHANGE;

	public static enum Mode {
		ACTIVATE, //
		CONFIG_CHANGE, //
		DEACTIVATE, //
	}

	private final Config configInvalid = ConfigFactory.empty("invalid");

	private Config configPast = configInvalid;

	protected Config configPast() {
		return configPast;
	}

	private Config configNext = configInvalid;

	protected Config configNext() {
		return configNext;
	}

	@Activate
	protected void activate() {

		log.debug("app-activate");

		process(Mode.ACTIVATE);

	}

	@Deactivate
	protected void deactivate() {

		log.debug("app-deactivate");

		process(Mode.DEACTIVATE);

	}

	@Modified
	protected void modified() {

		log.debug("app-modified");

	}

	@Override
	public void handleEvent(final Event event) {

		log.debug("event-topic : {}", event.getTopic());

		process(Mode.CONFIG_CHANGE);

	}

	protected boolean isConfigValid() {
		return configManager.isConfigValid();
	}

	protected void process(final Mode mode) {

		if (!isConfigValid()) {
			log.warn("config is invalid");
			return;
		}

		configNext = configManager.getConfig();

		log.debug("config : \n{} \n{}", mode, ConfigAny.toString(configNext));

		processChange(mode);

		configPast = configNext;
		configNext = configInvalid;

	}

	protected void processChange(final Mode mode) {
		log.error("expecting override", new Exception());
	}

	//

	private ConfigManager configManager;

	protected ConfigManager configManager() {
		return configManager;
	}

	@Reference
	protected void bind(final ConfigManager s) {
		configManager = s;
	}

	protected void unbind(final ConfigManager s) {
		configManager = null;
	}

}
