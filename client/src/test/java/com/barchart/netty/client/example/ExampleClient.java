/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.client.example;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rx.util.functions.Action1;

import com.barchart.netty.client.base.AuthenticatingConnectableBase;
import com.barchart.netty.client.policy.ReconnectPolicy;
import com.barchart.netty.client.transport.TransportProtocol;

/**
 * Basic example client constructed statically.
 */
public class ExampleClient extends
		AuthenticatingConnectableBase<ExampleClient, Object> {

	/**
	 ** USAGE EXAMPLE
	 **/

	public static void main(final String[] args) {

		/*
		 * Simple use case - no authentication, timeout or heartbeat.
		 */

		ExampleClient client =
				ExampleClient.builder().host("tcp://localhost:6497").build();

		/*
		 * More complex use case with authentication, heartbeat pings, read
		 * timeouts and an auto-reconnect policy.
		 */
		final ExampleClient.Builder builder =
				ExampleClient
						.builder()
						.host("tcp://localhost:6497")
						.timeout(30, TimeUnit.SECONDS)
						.ping(10, TimeUnit.SECONDS)
						.authenticator(new DummyAuthenticationHandler.Builder());

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

	protected static class Builder
			extends
			AuthenticatingConnectableBase.Builder<Builder, ExampleClient, Object> {

		@Override
		public ExampleClient build() {

			final ExampleClient client = new ExampleClient(transport);

			return super.configure(client);

		}
	}

	public static Builder builder() {
		return new Builder();
	}

	protected ExampleClient(final TransportProtocol transport_) {
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
