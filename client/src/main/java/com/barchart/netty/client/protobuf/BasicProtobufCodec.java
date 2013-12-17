package com.barchart.netty.client.protobuf;

import io.netty.buffer.MessageBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.barchart.netty.client.messages.Capabilities;
import com.barchart.netty.client.messages.Ping;
import com.barchart.netty.client.messages.Pong;
import com.barchart.netty.client.messages.StartTLS;
import com.barchart.netty.client.messages.StopTLS;
import com.barchart.netty.client.messages.Version;
import com.barchart.proto.buf.session.CapabilitiesMessage;
import com.barchart.proto.buf.session.SessionHeartbeatMessage;
import com.barchart.proto.buf.session.SessionTimestampMessage;
import com.barchart.proto.buf.session.StartTLSMessage;
import com.barchart.proto.buf.session.StopTLSMessage;

@Sharable
public class BasicProtobufCodec extends MessageToMessageCodec<Object, Object> {

	public BasicProtobufCodec() {
		super(CapabilitiesMessage.class, Capabilities.class);
	}

	@Override
	protected void encode(final ChannelHandlerContext ctx, final Object msg,
			final MessageBuf<Object> out) throws Exception {

		if (msg instanceof Capabilities) {
			out.add(encode((Capabilities) msg));
		} else if (msg instanceof Ping) {
			out.add(encode((Ping) msg));
		} else if (msg instanceof Pong) {
			out.add(encode((Pong) msg));
		} else if (msg instanceof StartTLS) {
			out.add(encode((StartTLS) msg));
		} else if (msg instanceof StopTLS) {
			out.add(encode((StopTLS) msg));
		} else {
			out.add(msg);
		}

	}

	@Override
	protected void decode(final ChannelHandlerContext ctx, final Object msg,
			final MessageBuf<Object> out) throws Exception {

		if (msg instanceof CapabilitiesMessage) {
			out.add(decode((CapabilitiesMessage) msg));
		} else if (msg instanceof SessionHeartbeatMessage) {
			out.add(decode((SessionHeartbeatMessage) msg));
		} else if (msg instanceof SessionTimestampMessage) {
			out.add(decode((SessionTimestampMessage) msg));
		} else if (msg instanceof StartTLSMessage) {
			out.add(decode((StartTLSMessage) msg));
		} else if (msg instanceof StopTLSMessage) {
			out.add(decode((StopTLSMessage) msg));
		} else {
			out.add(msg);
		}

	}

	private Capabilities decode(final CapabilitiesMessage msg) {

		final Set<String> capabilities = new HashSet<String>();

		if (msg.getCapabilityList().contains(
				CapabilitiesMessage.Capabilities.TLS)) {
			capabilities.add(Capabilities.ENC_TLS);
		}
		if (msg.getCapabilityList().contains(
				CapabilitiesMessage.Capabilities.PLAINTEXT)) {
			capabilities.add(Capabilities.ENC_NONE);
		}

		return new Capabilities() {

			@Override
			public Set<String> capabilities() {
				return Collections.unmodifiableSet(capabilities);
			}

			@Override
			public Version version() {
				return new Version("1");
			}

			@Override
			public Version minVersion() {
				return new Version("1");
			}

		};

	}

	private CapabilitiesMessage encode(final Capabilities msg) {

		final CapabilitiesMessage.Builder builder =
				CapabilitiesMessage.newBuilder();

		// Legacy stuff only supports TLS/PLAINTEXT notifification
		if (msg.capabilities().contains(Capabilities.ENC_TLS)) {
			builder.addCapability(CapabilitiesMessage.Capabilities.TLS);
		}
		if (msg.capabilities().contains(Capabilities.ENC_NONE)) {
			builder.addCapability(CapabilitiesMessage.Capabilities.PLAINTEXT);
		}

		return builder.build();

	}

	private Ping decode(final SessionHeartbeatMessage msg) {

		final long timestamp = msg.getLocalTimestamp();

		return new Ping() {
			@Override
			public long timestamp() {
				return timestamp;
			}
		};

	}

	private SessionHeartbeatMessage encode(final Ping msg) {

		return SessionHeartbeatMessage.newBuilder()
				.setLocalTimestamp(msg.timestamp()).build();

	}

	private Pong decode(final SessionTimestampMessage msg) {

		final long timestamp = msg.getLocalTimestamp();
		final long pinged = msg.getPeerTimestamp();

		return new Pong() {

			@Override
			public long timestamp() {
				return timestamp;
			}

			@Override
			public long pinged() {
				return pinged;
			}

		};

	}

	private SessionTimestampMessage encode(final Pong msg) {

		return SessionTimestampMessage.newBuilder()
				.setLocalTimestamp(msg.timestamp())
				.setPeerTimestamp(msg.pinged()).build();

	}

	private StartTLS decode(final StartTLSMessage msg) {
		return new StartTLS() {
		};
	}

	private StartTLSMessage encode(final StartTLS msg) {
		return StartTLSMessage.getDefaultInstance();
	}

	private StopTLS decode(final StopTLSMessage msg) {
		return new StopTLS() {
		};
	}

	private StopTLSMessage encode(final StopTLS msg) {
		return StopTLSMessage.getDefaultInstance();
	}

}
