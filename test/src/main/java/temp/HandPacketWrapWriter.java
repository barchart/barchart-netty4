package temp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;
import io.netty.channel.ChannelStateHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.barchart.netty.api.NettyDot;
import com.barchart.netty.util.point.NetAddress;
import com.barchart.netty.util.point.NetPoint;
import com.barchart.proto.buf.wrap.PacketWrapper;
import com.barchart.proto.buf.wrap.PacketWrapper.Builder;
import com.google.protobuf.ByteString;

/** record ByteBuf packets into file */
public class HandPacketWrapWriter extends ChannelStateHandlerAdapter implements
		ChannelInboundMessageHandler<Object> {

	@Override
	public MessageBuf<Object> newInboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		return Unpooled.messageBuffer();
	}

	protected String id;
	protected NetAddress localAddress;
	protected NetAddress remoteAddress;

	protected OutputStream output;

	private ScheduledFuture<?> future;

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {

		final NetPoint point =
				ctx.channel().attr(NettyDot.ATTR_NET_POINT).get();

		id = point.getId();
		localAddress = point.getLocalAddress();
		remoteAddress = point.getRemoteAddress();

		final String folder = point.getString("folder", "default-folder");
		final String file = //
				"record." + id + "." + System.currentTimeMillis() + ".buf";

		final File path = new File(folder, file);
		output = new BufferedOutputStream(new FileOutputStream(path));

		future =
				ctx.channel().eventLoop()
						.scheduleAtFixedRate(task, 3, 3, TimeUnit.SECONDS);

		super.channelActive(ctx);

	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx)
			throws Exception {

		future.cancel(true);

		output.close();

		super.channelInactive(ctx);

	}

	private final AtomicLong sequence = new AtomicLong(0);

	@Override
	public final void inboundBufferUpdated(final ChannelHandlerContext ctx)
			throws Exception {

		final MessageBuf<Object> source = ctx.inboundMessageBuffer();

		while (true) {

			final Object message = source.poll();

			if (message == null) {
				break;
			}

			if (message instanceof ByteBuf) {

				final ByteBuf buffer = (ByteBuf) message;
				final byte[] array = new byte[buffer.readableBytes()];
				buffer.readBytes(array);

				final Builder wrapper = PacketWrapper.newBuilder();

				wrapper.setSourceAddress(localAddress.intoTuple());
				wrapper.setTargetAddress(remoteAddress.intoTuple());

				wrapper.setSequence(sequence.getAndIncrement());
				wrapper.setTimeStamp(System.currentTimeMillis());
				wrapper.setBody(ByteString.copyFrom(array));

				wrapper.build().writeDelimitedTo(output);

			}

		}

	}

	private final Runnable task = new Runnable() {
		@Override
		public void run() {
			try {
				output.flush();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public void freeInboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		// TODO Auto-generated method stub
	}

}
