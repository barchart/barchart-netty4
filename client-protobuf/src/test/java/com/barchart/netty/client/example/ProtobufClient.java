package com.barchart.netty.client.example;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rx.util.functions.Action1;

import com.barchart.netty.client.policy.ReconnectPolicy;
import com.barchart.netty.client.protobuf.ProtobufClientBase;
import com.barchart.netty.client.transport.TransportProtocol;

public class ProtobufClient extends ProtobufClientBase<ProtobufClient> {

	/**
	 ** USAGE EXAMPLE
	 **/

	public static void main(final String[] args) {

		/*
		 * Simple use case - no authentication, timeout or heartbeat.
		 */

		ProtobufClient client =
				ProtobufClient.builder().host("tcp://localhost:6497").build();

		/*
		 * More complex use case with authentication, heartbeat pings, read
		 * timeouts and an auto-reconnect policy.
		 */
		final ProtobufClient.Builder builder =
				ProtobufClient
						.builder()
						.host("tcp://localhost:6497")
						.timeout(30, TimeUnit.SECONDS)
						.ping(10, TimeUnit.SECONDS)
						.credentials("bcnews", "bcnews".toCharArray(), "test",
								"netty-test");

		client = builder.build();

		client.stateChanges().subscribe(
				new ReconnectPolicy(Executors.newScheduledThreadPool(1), 5,
						TimeUnit.SECONDS));

		// Listen to state changes
		client.stateChanges().subscribe(statePrinter);
		client.authStateChanges().subscribe(authPrinter);

		// Initiate connection
		client.connect();

		// Watch the console output

	}

	protected static class Builder extends
			ProtobufClientBase.Builder<Builder, ProtobufClient> {

		@Override
		public ProtobufClient build() {

			final ProtobufClient client = new ProtobufClient(transport);

			return super.configure(client);

		}

	}

	public static Builder builder() {
		return new Builder();
	}

	protected ProtobufClient(final TransportProtocol transport_) {
		super(transport_);
	}

	final static Action1<AuthState> authPrinter = new Action1<AuthState>() {
		@Override
		public void call(final AuthState event) {
			System.out.println(event.getClass().getSimpleName() + " event: "
					+ event.toString());
		}
	};

	final static Action1<StateChange<?>> statePrinter =
			new Action1<StateChange<?>>() {
				@Override
				public void call(final StateChange<?> event) {
					System.out.println(event.state().getClass().getSimpleName()
							+ " event: " + event.state().toString());
				}
			};

}
