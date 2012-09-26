package com.barchart.netty.part.hand;

import io.netty.channel.ChannelHandlerAdapter;

import java.util.Map;
import java.util.UUID;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.host.api.NettyHand;

/** parent for "hand" - netty handlers */
@Component(name = HandAny.NAME, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class HandAny extends ChannelHandlerAdapter implements NettyHand {

	public static final String NAME = "barchart.netty.hand.any";

	@Override
	public String componentName() {
		return NAME;
	}

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final String instanceId = componentName() + "."
			+ UUID.randomUUID().toString();

	@Override
	public String componentInstance() {
		return instanceId;
	}

	@Activate
	protected void activate(final Map<String, String> props) throws Exception {

		log.debug("### activate : {}", props);

	}

	@Modified
	protected void modified(final Map<String, String> props) throws Exception {

		log.debug("### modified : {}", props);

	}

	@Deactivate
	protected void deactivate(final Map<String, String> props) throws Exception {

		log.debug("### deactivate : {}", props);

	}

}
