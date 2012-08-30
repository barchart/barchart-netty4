package com.barchart.netty.part.pipe;

import io.netty.channel.Channel;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.host.api.NettyHand;
import com.barchart.netty.host.api.NettyHandManager;
import com.barchart.netty.host.api.zzzNettyPipe;

/** parent for "pipe" - netty pipeline builders */
@Component(factory = zzzPipeAny.FACTORY)
public class zzzPipeAny implements zzzNettyPipe {

	public static final String FACTORY = "barchart.netty.pipe.any";

	@Override
	public String getFactoryId() {
		return FACTORY;
	}

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final String instanceId = getFactoryId() + "."
			+ UUID.randomUUID().toString();

	@Override
	public String getInstanceId() {
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

	@Override
	public List<Entry<String, NettyHand>> makePipe(final Channel channel) {

		final List<Entry<String, NettyHand>> list = new LinkedList<Entry<String, NettyHand>>();

		return list;

	}

	//
	{

		final String id = null;
		final Map<String, String> props = null;

		final NettyHand handler = handlerManager().create(id, props);

		final Channel channel = null;

		channel.pipeline().addLast(handler);

	}

	//

	private NettyHandManager handlerManager;

	protected NettyHandManager handlerManager() {
		return handlerManager;
	}

	@Reference
	protected void bind(final NettyHandManager s) {
		handlerManager = s;
	}

	protected void unbind(final NettyHandManager s) {
		handlerManager = null;
	}

}
