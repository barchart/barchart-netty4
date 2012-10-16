package com.barchart.netty.test.arb;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.MessageLoggingHandler;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.part.hand.RedirectMessageReader;
import com.barchart.netty.part.pipe.PipeAny;
import com.barchart.netty.util.point.NetPoint;

/** use for arbiter target end points */
@Component(name = PipeArbTarget.TYPE, immediate = true)
public class PipeArbTarget extends PipeAny implements NameArb {

	public static final String TYPE = "barchart.netty.pipe.arbiter.target";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public void apply(final NetPoint netPoint, final Channel targetChannel,
			final Mode mode) {

		final ChannelPipeline targetPipeline = targetChannel.pipeline();

		/** build target pipeline */

		targetPipeline.addLast(LOGGER, new MessageLoggingHandler());

		targetPipeline.addLast(ARBITER, new HandArb());

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

		final ChannelHandler previous = sourcePipeline.get(ARBITER);

		if (previous == null) {
			log.error("invalid handler", new IllegalStateException(sourceId));
			return;
		}

		final ChannelHandler current = new RedirectMessageReader(targetPipeline);

		sourcePipeline.replace(previous, ARBITER, current);

		log.debug("arbiter mapping : \n\t source : {} \n\t target : {}",
				sourceDot, targetPoint);

	}

}
