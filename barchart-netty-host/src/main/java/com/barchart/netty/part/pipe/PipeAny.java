package com.barchart.netty.part.pipe;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.host.api.NettyDotManager;
import com.barchart.netty.host.api.NettyHandManager;
import com.barchart.netty.host.api.NettyPipe;

/** parent for "pipe" - netty pipeline builders */
@Component(name = PipeAny.TYPE, immediate = true)
public class PipeAny implements NettyPipe {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static final String TYPE = "barchart.netty.pipe.any";

	@Override
	public String type() {
		return TYPE;
	}

	@Activate
	protected void activate(final Map<String, String> props) throws Exception {

		log.debug("activate : {}", props);

	}

	@Modified
	protected void modified(final Map<String, String> props) throws Exception {

		log.debug("modified : {}", props);

	}

	@Deactivate
	protected void deactivate(final Map<String, String> props) throws Exception {

		log.debug("deactivate : {}", props);

	}

	//

	@Override
	public void apply(final NettyDot dot, final Channel channel, final Mode mode) {

		switch (mode) {
		case DEFAULT:
			applyDefault(dot, channel);
			break;
		case DERIVED:
			applyDerived(dot, channel);
			break;
		default:
			log.error("wrong mode", new Exception());
		}

	}

	protected void applyDefault(final NettyDot dot, final Channel channel) {

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("logger", new LoggingHandler());

	}

	protected void applyDerived(final NettyDot dot, final Channel channel) {

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("logger", new LoggingHandler());

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

	//

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
