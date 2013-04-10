package temp.arb;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.MessageLoggingHandler;

import java.util.List;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.api.NettyDot;
import com.barchart.netty.part.hand.RedirectMessageReader;
import com.barchart.netty.pipe.PipeAny;
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
	public void apply(final Channel targetChannel, final Mode mode) {

		final NetPoint netPoint = targetChannel.attr(NettyDot.ATTR_NET_POINT)
				.get();

		final ChannelPipeline targetPipeline = targetChannel.pipeline();

		/** build target pipeline */

		targetPipeline.addLast(LOGGER, new MessageLoggingHandler());

		targetPipeline.addLast(ARBITER, new HandArb());

		/** attach arbitrage sources */

		final List<String> sourceList = netPoint.getUniformList(
				"arbiter-source-list", String.class);

		for (final String sourceId : sourceList) {
			attachSource(sourceId, netPoint, targetPipeline);
		}

	}

	/**
	 * inject message redirect from source into target
	 */
	protected void attachSource(final String sourceId,
			final NetPoint targetPoint, final ChannelPipeline targetPipeline) {

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
