package com.barchart.netty.client.pipeline;

import io.netty.channel.ChannelHandlerContext;

import com.barchart.netty.client.messages.Capabilities;
import com.barchart.netty.client.messages.Version;
import com.barchart.netty.client.messages.VersionRequest;
import com.barchart.netty.client.messages.VersionResponse;
import com.barchart.netty.client.pipeline.VersionFlowHandler.VersionEvent;
import com.barchart.netty.client.pipeline.VersionFlowHandler.VersionState;
import com.barchart.util.flow.api.Context;
import com.barchart.util.flow.api.Event;
import com.barchart.util.flow.api.Flow;
import com.barchart.util.flow.api.Point;
import com.barchart.util.flow.api.State;
import com.barchart.util.flow.provider.Provider;

public class VersionFlowHandler extends MessageFlowHandler<VersionEvent, VersionState> {

	public static enum VersionEvent implements Event<VersionEvent> {
		CONNECTED, //
		CAPABILITIES, //
		SEND_VERSION, //
		VERSION_OK, //
		VERSION_FAILED
	}

	public static enum VersionState implements State<VersionState> {
		CONNECTED, //
		NEGOTIATING, //
		SETTING_VERSION, //
		COMPLETE, //
		FAILED
	}

	private final Version version;
	private final Flow<VersionEvent, VersionState, ChannelHandlerContext> flow;

	private Capabilities capabilities = null;
	private Context<VersionEvent, VersionState, ChannelHandlerContext> context =
			null;

	public VersionFlowHandler(final Version version_) {
		version = version_;
		flow = buildFlow();
	}

	private Flow<VersionEvent, VersionState, ChannelHandlerContext> buildFlow() {

		final Flow.Builder<VersionEvent, VersionState, ChannelHandlerContext> fb =
				Provider.flowBuilder(VersionEvent.class, VersionState.class);

		/*
		 * Initial states
		 */

		fb.initial(VersionState.CONNECTED);
		fb.initial(VersionEvent.CONNECTED);
		fb.terminal(VersionState.COMPLETE);
		fb.terminal(VersionEvent.VERSION_OK);

		/*
		 * State listeners
		 */

		fb.listener(new OnError());
		fb.at(VersionState.FAILED).listener(new OnFailed());
		fb.at(VersionState.COMPLETE).listener(new OnComplete());

		fb.at(VersionState.NEGOTIATING).listener(new Negotiating());

		/*
		 * State transitions
		 */

		// Received capabilities
		fb.at(VersionState.CONNECTED).on(VersionEvent.CAPABILITIES)
				.to(VersionState.NEGOTIATING);

		// Proto version
		fb.at(VersionState.NEGOTIATING).on(VersionEvent.VERSION_OK)
				.to(VersionState.COMPLETE);
		fb.at(VersionState.NEGOTIATING).on(VersionEvent.SEND_VERSION)
				.to(VersionState.SETTING_VERSION);
		fb.at(VersionState.SETTING_VERSION).on(VersionEvent.VERSION_OK)
				.to(VersionState.COMPLETE);
		fb.at(VersionState.SETTING_VERSION).on(VersionEvent.VERSION_FAILED)
				.to(VersionState.FAILED);

		fb.enforce(true);

		return fb.build();

	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
		context = flow.contextBuilder().build(ctx);
		super.channelActive(ctx);
	}

	@Override
	protected boolean messageReceived(final Object message)
			throws IllegalStateException {

		if (message instanceof Capabilities) {

			capabilities = (Capabilities) message;
			forwardOnComplete(capabilities);
			context.fire(VersionEvent.CAPABILITIES);
			return true;

		} else if (message instanceof VersionResponse) {

			if (((VersionResponse) message).success()) {
				context.fire(VersionEvent.VERSION_OK);
			} else {
				context.fire(VersionEvent.VERSION_FAILED);
			}

			return true;

		}

		// No extra messages allowed until negotiation finishes
		throw new IllegalStateException("Unexpected message type: "
				+ message.getClass());

	}

	private class Negotiating extends StateTransition {

		@Override
		public void enter(
				final Point<VersionEvent, VersionState> past,
				final Point<VersionEvent, VersionState> next,
				final Context<VersionEvent, VersionState, ChannelHandlerContext> context)
				throws Exception {

			if (capabilities == null) {
				throw new IllegalStateException("Capabilities not received yet");
			}

			if (version == null) {

				// Assume latest version OK
				context.fire(VersionEvent.VERSION_OK);

			} else {

				// Requested version not in valid range
				if (!version.inRange(capabilities.minVersion(),
						capabilities.version())) {
					throw new UnsupportedOperationException(
							"Remote peer does not support requested protocol version: "
									+ version.toString());
				}

				// Request older protocol version
				context.attachment().write(new VersionRequest() {
					@Override
					public Version version() {
						return version;
					}
				});

				// Advance state machine
				context.fire(VersionEvent.SEND_VERSION);

			}

		}

	}

}
