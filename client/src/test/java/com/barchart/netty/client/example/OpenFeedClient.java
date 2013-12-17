package com.barchart.netty.client.example;

import io.netty.channel.EventLoopGroup;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rx.util.functions.Action1;

import com.barchart.netty.client.base.ReconnectPolicy;
import com.barchart.netty.client.openfeed.OpenFeedClientBase;
import com.barchart.netty.client.transport.TransportProtocol;

public class OpenFeedClient extends OpenFeedClientBase<OpenFeedClient> {

	protected static class Builder extends
			OpenFeedClientBase.Builder<Builder, OpenFeedClient> {

		@Override
		public OpenFeedClient build() {

			final OpenFeedClient client =
					new OpenFeedClient(eventLoop, transport);

			return super.configure(client);

		}

	}

	public static Builder builder() {
		return new Builder();
	}

	protected OpenFeedClient(final EventLoopGroup eventLoop_,
			final TransportProtocol transport_) {
		super(eventLoop_, transport_);
	}

	/**
	 ** USAGE EXAMPLE
	 **/

	public static void main(final String[] args) {

		final OpenFeedClient client =
				OpenFeedClient
						.builder()
						.host("ws://localhost/handler")
						.ping(10, TimeUnit.SECONDS)
						.timeout(30, TimeUnit.SECONDS)
						.credentials(URI.create("client://barchart.com/user"),
								"pass".toCharArray()) //
						.build();

		// Reconnection policy
		client.stateChanges().subscribe(
				new ReconnectPolicy(Executors.newScheduledThreadPool(1), 5,
						TimeUnit.SECONDS));

		client.stateChanges().subscribe(eventPrinter);
		client.authStateChanges().subscribe(eventPrinter);

		client.connect();

	}

	final static Action1<Object> eventPrinter = new Action1<Object>() {
		@Override
		public void call(final Object event) {
			System.out.println(event.getClass().getSimpleName() + " event: "
					+ event.toString());
		}
	};

}
