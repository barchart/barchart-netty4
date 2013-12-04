package temp.arb;

import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;
import io.netty.channel.ChannelStateHandlerAdapter;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openfeed.proto.generic.Packet;

import com.barchart.netty.api.NettyDot;
import com.barchart.netty.util.arb.Arbiter;
import com.barchart.netty.util.arb.ArbiterCore;
import com.barchart.netty.util.point.NetPoint;

/**
 * duplicate message arbiter handler
 */
public class HandArb extends ChannelStateHandlerAdapter implements
		ChannelInboundMessageHandler<Packet> {

	@Override
	public MessageBuf<Packet> newInboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		return Unpooled.messageBuffer();
	}

	private ChannelHandlerContext ctx;

	private int arbiterDepth;
	private int arbiterTimeout;
	private final TimeUnit arbiterUnit = TimeUnit.MILLISECONDS;

	private Arbiter<Object> arbiter;

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {

		this.ctx = ctx;

		final NetPoint point = ctx.channel().attr(NettyDot.ATTR_NET_POINT)
				.get();

		arbiterDepth = point.getInt("arbiter-depth", 10 * 1000);
		arbiterTimeout = point.getInt("arbiter-timeout", 200);

		arbiter = new ArbiterCore<Object>(arbiterDepth);

		super.channelActive(ctx);

	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx)
			throws Exception {

		super.channelInactive(ctx);

	}

	@Override
	public final void inboundBufferUpdated(final ChannelHandlerContext ctx)
			throws Exception {

		final MessageBuf<Packet> source = ctx.inboundMessageBuffer();

		while (true) {

			final Packet message = source.poll();

			if (message == null) {
				break;
			}

			final long sequence = message.getSequence();

			arbiter.fill(sequence, message);

		}

		if (arbiter.isReady()) {

			timerOff();

			drain();

		} else {

			timerOn();

		}

	}

	private void timerOn() {

		if (future == null || future.isDone()) {
			future = ctx.channel().eventLoop()
					.schedule(task, arbiterTimeout, arbiterUnit);
		}

	}

	private void timerOff() {

		if (future == null || future.isDone()) {
			return;
		}

		future.cancel(true);
		future = null;

	}

	private ScheduledFuture<?> future;

	private final Runnable task = new Runnable() {
		@Override
		public void run() {
			drain();
		}
	};

	private void drain() {

		final MessageBuf<Object> target = ctx.nextInboundMessageBuffer();

		arbiter.drainTo(target);

		ctx.fireInboundBufferUpdated();

	}

}
