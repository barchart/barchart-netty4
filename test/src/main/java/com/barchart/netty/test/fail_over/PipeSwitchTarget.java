package com.barchart.netty.test.fail_over;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.MessageLoggingHandler;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.api.NettyDot;
import com.barchart.netty.part.hand.RedirectMessageReader;
import com.barchart.netty.pipe.PipeAny;
import com.barchart.netty.util.point.NetPoint;

/** use for switch target end points */
@Component(name = PipeSwitchTarget.TYPE, immediate = true)
public class PipeSwitchTarget extends PipeAny implements NameSwitch {

	public static final String TYPE = "barchart.netty.pipe.switch.target";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public void apply(final Channel targetChannel, final Mode mode) {

		final NetPoint netPoint = targetChannel.attr(NettyDot.ATTR_NET_POINT)
				.get();

		final ChannelPipeline targetPipeline = targetChannel.pipeline();

		/** build target pipeline */

		targetPipeline.addLast(LOGGER, new MessageLoggingHandler());

		targetPipeline.addLast(SWITCH, new HandSwitch());

		/** attach arbitrage sources */

		attachSource("source-one", netPoint, targetPipeline);

		attachSource("source-two", netPoint, targetPipeline);

	}

	/**
	 * inject message redirect from source into target
	 */
	protected void attachSource(final String pointKey,
			final NetPoint targetPoint, final ChannelPipeline targetPipeline) {

		final String sourceId = targetPoint.getString(pointKey,
				"invalid-source");

		final NettyDot sourceDot = channelManager().instance(sourceId);

		if (sourceDot == null) {
			log.error("missing source", new IllegalStateException(sourceId));
			return;
		}

		final ChannelPipeline sourcePipeline = sourceDot.channel().pipeline();

		final ChannelHandler previous = sourcePipeline.get(SWITCH);

		if (previous == null) {
			log.error("invalid handler", new IllegalStateException(sourceId));
			return;
		}

		final ChannelHandler current = new RedirectMessageReader(targetPipeline);

		sourcePipeline.replace(previous, SWITCH, current);

		log.debug("arbiter mapping : \n\t source : {} \n\t target : {}",
				sourceDot, targetPoint);

	}

}
