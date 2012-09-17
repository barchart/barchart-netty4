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
@Component(name = PipeArbTarget.NAME, immediate = true)
public class PipeArbTarget extends PipeAny implements NameArb {

	public static final String NAME = "barchart.netty.pipe.arbiter.target";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void apply(final NettyDot targetDot, final Channel targetChannel) {

		final ChannelPipeline targetPipeline = targetChannel.pipeline();

		/** build target pipeline */

		targetPipeline.addLast(LOGGER, new MessageLoggingHandler());

		targetPipeline.addLast(ARBITER, new HandArb());

		/** attach arbitrage sources */

		attachSource("source-one", targetDot, targetPipeline);

		attachSource("source-two", targetDot, targetPipeline);

	}

	/**
	 * inject message redirect from source into target
	 */
	protected void attachSource(final String pointKey,
			final NettyDot targetDot, final ChannelPipeline targetPipeline) {

		final NetPoint targetPoint = targetDot.getNetPoint();

		final String sourceId = targetPoint.load(pointKey);

		final NettyDot sourceDot = channelManager().instance(sourceId);

		if (sourceDot == null) {
			log.error("missing source", new IllegalStateException(sourceId));
			return;
		}

		final ChannelPipeline sourcePipeline = sourceDot.getChannel()
				.pipeline();

		final ChannelHandler previous = sourcePipeline.get(ARBITER);

		if (previous == null) {
			log.error("invalid handler", new IllegalStateException(sourceId));
			return;
		}

		final ChannelHandler current = new RedirectMessageReader(targetPipeline);

		sourcePipeline.replace(previous, ARBITER, current);

		log.debug("arbiter mapping : \n\t source : {} \n\t target : {}",
				sourceDot, targetDot);

	}

}
