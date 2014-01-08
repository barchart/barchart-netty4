package com.barchart.netty.client.protobuf;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openfeed.proto.generic.Packet;
import org.openfeed.proto.generic.PacketType;

import com.barchart.account.api.Account;
import com.barchart.account.api.Account.Scheme;
import com.barchart.account.api.AuthResult;
import com.barchart.account.api.Permissions;
import com.barchart.account.common.DefaultEditableAccount;
import com.barchart.account.common.DefaultPermission;
import com.barchart.account.common.DefaultPermissions;
import com.barchart.account.common.request.AuthRequest;
import com.barchart.netty.client.messages.Capabilities;
import com.barchart.netty.client.messages.Ping;
import com.barchart.netty.client.messages.Pong;
import com.barchart.netty.client.messages.StartTLS;
import com.barchart.netty.client.messages.StopTLS;
import com.barchart.netty.client.messages.Version;
import com.barchart.proto.buf.session.AuthRequestMessage;
import com.barchart.proto.buf.session.AuthResponseMessage;
import com.barchart.proto.buf.session.CapabilitiesMessage;
import com.barchart.proto.buf.session.ProductMessage;
import com.barchart.proto.buf.session.SessionHeartbeatMessage;
import com.barchart.proto.buf.session.SessionPacketMessage;
import com.barchart.proto.buf.session.SessionTimestampMessage;
import com.barchart.proto.buf.session.StartTLSMessage;
import com.barchart.proto.buf.session.StopTLSMessage;
import com.barchart.proto.buf.session.UserMessage;
import com.google.protobuf.MessageLite;

@Sharable
public class BasicProtobufCodec extends MessageToMessageCodec<Object, Object> {

	@Override
	@SuppressWarnings("unchecked")
	protected void encode(final ChannelHandlerContext ctx, final Object msg,
			final List<Object> out) throws Exception {

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
		} else if (msg instanceof AuthResult) {
			out.add(encode((AuthResult<Account>) msg));
		} else if (msg instanceof AuthRequest) {
			out.add(encode((AuthRequest) msg));
		} else {
			out.add(msg);
		}

	}

	@Override
	protected void decode(final ChannelHandlerContext ctx, final Object msg,
			final List<Object> out) throws Exception {

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
		} else if (msg instanceof AuthRequestMessage) {
			out.add(decode((AuthRequestMessage) msg));
		} else if (msg instanceof AuthResponseMessage) {
			// Multi message
			decode((AuthResponseMessage) msg, out);
		} else {
			out.add(msg);
		}

	}

	private Capabilities decode(final CapabilitiesMessage msg) {

		final Set<String> capabilities = new HashSet<String>();

		// All protobuf hosts support plaintext auth for backwards compatibility
		capabilities.add(Capabilities.AUTH_PASSWORD);

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

	private Packet encode(final Capabilities msg) {

		final CapabilitiesMessage.Builder builder =
				CapabilitiesMessage.newBuilder();

		// Legacy stuff only supports TLS/PLAINTEXT notifification
		if (msg.capabilities().contains(Capabilities.ENC_TLS)) {
			builder.addCapability(CapabilitiesMessage.Capabilities.TLS);
		}
		if (msg.capabilities().contains(Capabilities.ENC_NONE)) {
			builder.addCapability(CapabilitiesMessage.Capabilities.PLAINTEXT);
		}

		return wrap(builder.build(), SessionPacketMessage.Type.CAPABILITIES);

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

	private Packet encode(final Ping msg) {

		return wrap(
				SessionHeartbeatMessage.newBuilder()
						.setLocalTimestamp(msg.timestamp()).build(),
				SessionPacketMessage.Type.SESSION_HEARTBEAT);

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

	private Packet encode(final Pong msg) {

		return wrap(
				SessionTimestampMessage.newBuilder()
						.setLocalTimestamp(msg.timestamp())
						.setPeerTimestamp(msg.pinged()).build(),
				SessionPacketMessage.Type.TIMESTAMP);

	}

	private StartTLS decode(final StartTLSMessage msg) {
		return new StartTLS() {
		};
	}

	private Packet encode(final StartTLS msg) {

		return wrap(StartTLSMessage.getDefaultInstance(),
				SessionPacketMessage.Type.START_TLS);

	}

	private StopTLS decode(final StopTLSMessage msg) {
		return new StopTLS() {
		};
	}

	private Packet encode(final StopTLS msg) {

		return wrap(StopTLSMessage.getDefaultInstance(),
				SessionPacketMessage.Type.STOP_TLS);

	}

	private AuthRequest decode(final AuthRequestMessage msg) {

		final AuthRequest req = new AuthRequest();
		req.scheme(Scheme.CLIENT.name());
		req.username(msg.getUsername());
		req.secret(msg.getPassword().toCharArray());
		req.domain(msg.getGroup());

		return req;

	}

	private Packet encode(final AuthRequest msg) {

		return wrap(AuthRequestMessage.newBuilder().setUsername(msg.username())
				.setGroup(msg.domain()).setPassword(new String(msg.secret()))
				.build(), SessionPacketMessage.Type.AUTH_REQUEST);

	}

	private void decode(final AuthResponseMessage msg, final List<Object> out) {

		DefaultEditableAccount account = null;

		if (msg.hasUser()) {

			String group = msg.getUser().getGroup();
			if (group == null || group.isEmpty()) {
				group = "barchart.com";
			}

			final URI uri =
					URI.create("client://" + group + "/"
							+ msg.getUser().getUsername());

			// No support for IDs right now
			account =
					new DefaultEditableAccount(msg.getUser().getUsername(), uri);

		}

		final Account finalAccount = account;

		out.add(new AuthResult<Account>() {

			@Override
			public AuthResult.Status status() {
				return msg.getStatus() == AuthResponseMessage.Status.SUCCESS ? AuthResult.Status.AUTHENTICATED
						: AuthResult.Status.INVALID;
			}

			@Override
			public Account account() {
				return finalAccount;
			}

		});

		// Also send permissions if included

		final Permissions perms = new DefaultPermissions();
		for (final ProductMessage pm : msg.getUser().getProductList()) {
			perms.set(new DefaultPermission("com.barchart.banker.service."
					+ pm.getId()).set(pm.getMaxSymbols()));
		}
		for (final String perm : msg.getUser().getPermissionList()) {
			perms.set(new DefaultPermission("com.barchart.banker.product."
					+ perm).set(true));
		}

		if (!perms.map().isEmpty()) {
			out.add(perms);
		}

	}

	private Packet encode(final AuthResult<Account> msg) {

		final AuthResponseMessage.Builder builder =
				AuthResponseMessage
						.newBuilder()
						.setStatus(
								msg.status() == AuthResult.Status.AUTHENTICATED ? AuthResponseMessage.Status.SUCCESS
										: AuthResponseMessage.Status.FAILURE);

		if (msg.account() != null) {
			builder.setUser(UserMessage.newBuilder()
					.setId(msg.account().id().id())
					.setUsername(msg.account().username())
					.setGroup(msg.account().domain()).build());
		}

		return wrap(builder.build(), SessionPacketMessage.Type.AUTH_RESPONSE);

	}

	private Packet wrap(final MessageLite message,
			final SessionPacketMessage.Type type) {

		return Packet.newBuilder().setType(PacketType.SESSION)
				.addBody(SessionPacketMessage.newBuilder() //
						.setType(type) //
						.setBody(message.toByteString()) //
						.build() //
						.toByteString()) //
				.build();

	}

}