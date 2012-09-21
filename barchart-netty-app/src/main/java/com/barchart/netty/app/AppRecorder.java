package com.barchart.netty.app;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.host.api.NettyDot;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

@Component(enabled = false, immediate = true)
public class AppRecorder extends AppBase {

	@Override
	protected void processChange(final Mode mode, final Config config) {

		final ConfigList sourceList = config.getList("feed-source.list");

		processChange(mode, sourceList);

	}

	protected void processChange(final Mode mode, final ConfigList list) {

		for (final ConfigValue value : list) {

			final Config entry = ((ConfigObject) value).toConfig().withValue(
					"pipeline",
					ConfigValueFactory
							.fromAnyRef("barchart.netty.pipe.record.wrapper"));

			switch (mode) {

			case ACTIVATE:

				final NettyDot dot = channelManager().create(entry);

				if (dot == null) {
					log.error("failed to create", new Exception("" + entry));
					continue;
				}

				break;

			case CONFIG_CHANGE:

				// XXX

				break;

			case DEACTIVATE:

				final boolean is = channelManager().destroy(entry);

				if (!is) {
					log.error("failed to destroy", new Exception("" + entry));
					continue;
				}

				break;
			}

		}

	}

}
