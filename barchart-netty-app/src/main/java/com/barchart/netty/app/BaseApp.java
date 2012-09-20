package com.barchart.netty.app;

import org.osgi.service.component.annotations.Activate;
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
import com.barchart.netty.host.api.NettyDotManager;
import com.typesafe.config.Config;

public class BaseApp implements EventHandler {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	/** provide event subscription topic filter */
	@Property(name = EventConstants.EVENT_TOPIC)
	static final String TOPIC = ConfigEvent.CONFIG_CHANGE;

	public static enum Mode {
		ACTIVATE, //
		CONFIG_CHANGE, //
		DEACTIVATE, //
	}

	@Activate
	protected void activate() {

		log.info("config-activate");

		processChange(Mode.ACTIVATE);

	}

	@Deactivate
	protected void deactivate() {

		log.info("config-deactivate");

		processChange(Mode.DEACTIVATE);

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

		processChange(Mode.CONFIG_CHANGE);

	}

	protected void processChange(final Mode mode) {

		if (!manager.isConfigValid()) {
			log.warn("config is invalid");
			return;
		}

		log.info("\n{}", mode);

		final Config config = manager.getConfig();

		log.info("config : \n{}", ConfigAny.toString(config));

		processChange(mode, config);

	}

	protected void processChange(final Mode mode, final Config config) {
		log.error("expecting override", new Exception());
	}

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
