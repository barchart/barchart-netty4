package com.barchart.netty.client.sandbox;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.ByteLoggingHandler;
import io.netty.handler.logging.MessageLoggingHandler;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;

import com.barchart.netty.client.Connectable;
import com.barchart.netty.client.base.ConnectableBase;
import com.barchart.netty.client.transport.TransportProtocol;
import com.barchart.proto.streamingfeed.BarchartMessageType;
import com.barchart.proto.streamingfeed.SubscriptionRequest;

public class WebsocketClient extends ConnectableBase<WebsocketClient> {

	/**
	 ** USAGE EXAMPLE
	 **/

	public static void main(final String[] args) {

		final WebsocketClient client =
				WebsocketClient
						.builder()
						.host("ws://ds1.dataserver.aws.barchart.com:6782/websocket")
						.timeout(30, TimeUnit.SECONDS).build();

		client.receive(Object.class).subscribe(new Observer<Object>() {

			@Override
			public void onNext(final Object msg) {
				System.out.println("Received: " + msg);
			}

			@Override
			public void onCompleted() {
			}

			@Override
			public void onError(final Throwable e) {
			}

		});

		client.stateChanges().subscribe(
				new Observer<Connectable.StateChange<WebsocketClient>>() {

					@Override
					public void onNext(
							final Connectable.StateChange<WebsocketClient> change) {

						System.out.println(change.state().getClass()
								.getSimpleName()
								+ " event: " + change.state().toString());

						if (change.state() == State.CONNECTED) {

							final SubscriptionRequest req =
									SubscriptionRequest.newBuilder()
											.setSymbol("ESZ13").build();

							final ByteBuf buf = Unpooled.buffer();

							buf.writeShort(BarchartMessageType.SUBSCRIPTION_REQUEST
									.ordinal());
							buf.writeBytes(req.toByteArray());
							buf.retain();

							System.out.println(Arrays.toString(buf.array()));

							client.send(buf);

						}

					}

					@Override
					public void onCompleted() {
					}

					@Override
					public void onError(final Throwable e) {
					}

				});

		// Initiate connection
		client.connect();

		// Watch the console output

	}

	public static class Builder extends
			ConnectableBase.Builder<Builder, WebsocketClient> {

		@Override
		public WebsocketClient build() {

			final WebsocketClient client = new WebsocketClient(transport);

			return super.configure(client);

		}

	}

	public static Builder builder() {
		return new Builder();
	}

	protected WebsocketClient(final TransportProtocol transport_) {
		super(transport_);
	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {

		pipeline.addFirst(new MessageLoggingHandler());
		pipeline.addFirst(new ByteLoggingHandler());

	}

	@Override
	public <U> Observable<U> receive(final Class<U> type) {
		return super.receive(type);
	}

	@Override
	public <U> Observable<U> send(final U message) {
		return super.send(message);
	}

}
