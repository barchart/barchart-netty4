package com.barchart.netty.part.dot;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.host.api.NettyGroup;
import com.barchart.netty.host.api.NettyPipe;
import com.barchart.netty.host.api.NettyPipeManager;
import com.barchart.netty.util.point.NetAddress;
import com.barchart.netty.util.point.NetPoint;

/**
 * parent for "dot" (end point) netty components
 */
@Component(name = DotAny.NAME, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class DotAny implements NettyDot {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static final String NAME = "barchart.netty.dot.any";

	@Override
	public String componentName() {
		return NAME;
	}

	private NetPoint netPoint;

	@Override
	public String componentInstance() {
		return netPoint.getId();
	}

	@Override
	public NetPoint netPoint() {
		return netPoint;
	}

	@Override
	public Channel channel() {
		throw new IllegalStateException("expecting override");
	}

	//

	/**
	 * builder for transient pipeline applicator handler
	 */
	protected final ChannelInitializer<Channel> pipeApply(
			final NettyPipe.Mode mode) {
		return new ChannelInitializer<Channel>() {
			@Override
			public void initChannel(final Channel channel) throws Exception {

				/** always link channel with owner dot */
				channel.attr(ATTR_NETTY_DOT).set(DotAny.this);

				final NettyPipe pipe = pipeManager().findPipe(pipeName());

				if (pipe == null) {

					log.error("missing pipeline", //
							new IllegalArgumentException(pipeName()));

				} else {

					pipe.apply(DotAny.this, channel, mode);

				}

			}
		};
	}

	//

	/** pipeline builder name */
	protected String pipeName() {
		return netPoint().getPipeline();
	}

	/** net point local address */
	protected NetAddress localAddress() {
		return netPoint().getLocalAddress();
	}

	/** net point remote address */
	protected NetAddress remoteAddress() {
		return netPoint().getRemoteAddress();
	}

	/** bootstrap startup */
	protected void activateBoot() throws Exception {
		//
	}

	/** bootstrap shutdown */
	protected void deactivateBoot() throws Exception {
		//
	}

	//

	@Activate
	protected void activate(final Map<String, String> props) throws Exception {

		log.debug("activate : {}", props);

		netPoint = NetPoint.from(props.get(PROP_NET_POINT));

		activateBoot();

	}

	@Modified
	protected void modified(final Map<String, String> props) throws Exception {

		log.debug("modified : {}", props);

		final NetPoint newPoint = NetPoint.from(props.get(PROP_NET_POINT));

		if (netPoint.equals(newPoint)) {
			return;
		}

		deactivateBoot();

		netPoint = newPoint;

		activateBoot();

	}

	@Deactivate
	protected void deactivate(final Map<String, String> props) throws Exception {

		log.debug("deactivate : {}", props);

		deactivateBoot();

		netPoint = null;

	}

	//

	private EventLoopGroup group;

	protected EventLoopGroup group() {
		return group;
	}

	@Reference
	protected void bind(final NettyGroup s) {
		group = s.getGroup();
	}

	protected void unbind(final NettyGroup s) {
		group = null;
	}

	//

	private NettyPipeManager pipeManager;

	protected NettyPipeManager pipeManager() {
		return pipeManager;
	}

	@Reference
	protected void bind(final NettyPipeManager s) {
		pipeManager = s;
	}

	protected void unbind(final NettyPipeManager s) {
		pipeManager = null;
	}

	//

	@Override
	public String toString() {
		return "dot : " + netPoint();
	}

}
