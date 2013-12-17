package com.barchart.netty.client.example;

import io.netty.channel.EventLoopGroup;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rx.util.functions.Action1;

import com.barchart.netty.client.base.ReconnectPolicy;
import com.barchart.netty.client.protobuf.ProtobufClientBase;
import com.barchart.netty.client.transport.TransportProtocol;

public class ProtobufClient extends ProtobufClientBase<ProtobufClient> {

	protected static class Builder extends
			ProtobufClientBase.Builder<Builder, ProtobufClient> {

		@Override
		public ProtobufClient build() {

			final ProtobufClient client =
					new ProtobufClient(eventLoop, transport);

			return super.configure(client);

		}

	}

	public static Builder builder() {
		return new Builder();
	}

	protected ProtobufClient(final EventLoopGroup eventLoop_,
			final TransportProtocol transport_) {
		super(eventLoop_, transport_);
	}

	/**
	 ** USAGE EXAMPLE
	 **/

	public static void main(final String[] args) {

		final ProtobufClient client = ProtobufClient.builder() //
				.host("tcp://news.aws.barchart.com:6497") //
				.timeout(30, TimeUnit.SECONDS) //
				.ping(10, TimeUnit.SECONDS) //
				.credentials("bcnews", "bcnews") //
				.build();

		// Reconnection policy
		client.stateChanges().subscribe(
				new ReconnectPolicy(Executors.newScheduledThreadPool(1), 5,
						TimeUnit.SECONDS));

		client.stateChanges().subscribe(statePrinter);
		client.authStateChanges().subscribe(authPrinter);

		client.connect();

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
					System.out.println(event.getClass().getSimpleName()
							+ " event: " + event.state().toString());
				}
			};

}
