package com.barchart.netty.dot;

import io.netty.channel.EventLoopGroup;

import java.util.Map;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.host.api.NettyGroup;
import com.barchart.netty.util.point.NetPoint;
import com.typesafe.config.ConfigFactory;

public class DotBase implements NettyDot {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private String instanceId;

	private NetPoint netPoint;

	@Override
	public String getInstanceId() {
		return instanceId;
	}

	@Override
	public NetPoint getNetPoint() {
		return netPoint;
	}

	@Activate
	protected void activate(final Map<String, String> props) {

		// log.debug("### props : {}", props);

		instanceId = props.get(Constants.SERVICE_PID);

		log.debug("### instanceId : {}", instanceId);

		final String pointConfig = props.get(PROP_NET_POINT_CONIFG);

		netPoint = NetPoint.from(ConfigFactory.parseString(pointConfig));

		log.debug("### netPoint.local : {}", netPoint.getLocalAddress());
		log.debug("### netPoint.remote : {}", netPoint.getRemoteAddress());

	}

	@Deactivate
	protected void deactivate(final Map<String, String> props) {

	}

	private EventLoopGroup group;

	protected EventLoopGroup getGroup() {
		return group;
	}

	@Reference
	protected void bind(final NettyGroup s) {
		group = s.getGroup();
	}

	protected void unbind(final NettyGroup s) {
		group = null;
	}

}
