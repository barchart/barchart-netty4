package com.barchart.netty.part.dot;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
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
@Component(factory = DotAny.FACTORY)
public class DotAny implements NettyDot {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static final String FACTORY = "barchart.netty.dot.any";

	@Override
	public String getFactoryId() {
		return FACTORY;
	}

	private NetPoint netPoint;

	@Override
	public String getInstanceId() {
		return netPoint.getId();
	}

	@Override
	public NetPoint getNetPoint() {
		return netPoint;
	}

	@Override
	public Channel getChannel() {
		return channel();
	}

	protected Channel channel() {
		throw new IllegalStateException("expecting override");
	}

	//

	/**
	 * default / parent
	 * 
	 * builder for transient pipeline applicator handler
	 */
	protected ChannelInitializer<Channel> pipeApply() {
		return new ChannelInitializer<Channel>() {
			@Override
			public void initChannel(final Channel channel) throws Exception {

				final NettyPipe pipe = pipeManager().findPipe(pipeName());

				if (pipe == null) {
					log.error("missing pipeline", //
							new IllegalArgumentException(pipeName()));
					return;
				}

				pipe.apply(DotAny.this, channel);

			}
		};
	}

	/**
	 * derived / child
	 * 
	 * builder for transient pipeline applicator handler
	 */
	protected ChannelInitializer<Channel> pipeApplyChild() {
		return new ChannelInitializer<Channel>() {
			@Override
			public void initChannel(final Channel channel) throws Exception {

				final NettyPipe pipe = pipeManager().findPipe(pipeName());

				if (pipe == null) {
					log.error("missing pipeline", //
							new IllegalArgumentException(pipeName()));
					return;
				}

				pipe.applyChild(DotAny.this, channel);

			}
		};
	}

	//

	/** pipeline builder name */
	protected String pipeName() {
		return getNetPoint().getPipeline();
	}

	/** net point local address */
	protected NetAddress localAddress() {
		return getNetPoint().getLocalAddress();
	}

	/** net point remote address */
	protected NetAddress remoteAddress() {
		return getNetPoint().getRemoteAddress();
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
		return "dot : " + getNetPoint();
	}

}
