package com.barchart.netty.part.dot;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.conf.util.BaseComponentImpl;
import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.host.api.NettyGroup;
import com.barchart.netty.host.api.NettyPipe;
import com.barchart.netty.host.api.NettyPipeManager;
import com.barchart.netty.util.point.NetAddress;
import com.barchart.netty.util.point.NetPoint;

/**
 * parent for "dot" (end point) netty components
 */
@Component(name = DotAny.TYPE, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class DotAny extends BaseComponentImpl implements NettyDot {

	public static final String TYPE = "barchart.netty.dot.any";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private NetPoint netPoint;

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
	protected void bootActivate() throws Exception {
		//
	}

	/** bootstrap shutdown */
	protected void bootDeactivate() throws Exception {
		//
	}

	//

	@Override
	protected void processActivate() throws Exception {

		netPoint = NetPoint.from(configCurrent());

		bootActivate();

	}

	@Override
	protected void processDeactivate() throws Exception {

		bootDeactivate();

		netPoint = null;

	}

	@Override
	protected void processModified() throws Exception {

		if (isConfigEqual()) {
			return;
		}

		bootDeactivate();

		netPoint = NetPoint.from(configCurrent());

		bootActivate();

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
