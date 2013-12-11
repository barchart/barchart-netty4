package com.barchart.netty.client.example;

import io.netty.channel.EventLoopGroup;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.util.functions.Action1;

import com.barchart.account.api.Account;
import com.barchart.account.api.AuthResult;
import com.barchart.netty.client.base.AuthenticatingConnectableBase;
import com.barchart.netty.client.base.ProtobufClientBase;
import com.barchart.netty.client.transport.TransportProtocol;

public class ExampleClient extends ProtobufClientBase<ExampleClient> {

	public static class Builder
			extends
			AuthenticatingConnectableBase.Builder<Builder, ExampleClient, Account> {

		@Override
		public ExampleClient build() {

			final ExampleClient client =
					new ExampleClient(eventLoop, address, transport);

			client.heartbeatInterval(10);

			return super.configure(client);

		}

	}

	public static Builder builder() {
		return new Builder();
	}

	protected ExampleClient(final EventLoopGroup eventLoop_,
			final InetSocketAddress address_, final TransportProtocol transport_) {

		super(eventLoop_, address_, transport_);

	}

	/**
	 ** USAGE EXAMPLE
	 **/

	public static void main(final String[] args) {

		final ExampleClient client = ExampleClient.builder() //
				.websocket(URI.create("ws://localhost/handler")) //
				.reconnect(5, TimeUnit.SECONDS) //
				.timeout(30, TimeUnit.SECONDS) //
				.authenticator(new Authenticator<Account>() {

					@Override
					public Observable<AuthResult<Account>> authenticate(
							final MessageStream stream) {

						// Do some authentication communication and return a
						// result
						return null;

					}

				}).build();

		client.stateChanges().subscribe(new Action1<State>() {
			@Override
			public void call(
					final com.barchart.netty.client.Connectable.State state) {
				System.out.println("connect state changed: " + state.name());
			}
		});

		client.connect();

	}

}
