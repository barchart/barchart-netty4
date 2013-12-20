package com.barchart.netty.client.sandbox;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.ByteLoggingHandler;

import java.io.ByteArrayOutputStream;

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
						.build();

		client.receive(ByteBuf.class).subscribe(new Observer<ByteBuf>() {

			@Override
			public void onNext(final ByteBuf msg) {
				try {
					final byte[] content = new byte[msg.readableBytes()];
					msg.readBytes(content);
					System.out.println("Received: " + content);
				} catch (final Throwable t) {
					t.printStackTrace();
				}
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

							client.channel.pipeline().addFirst(
									new ByteLoggingHandler());

							final SubscriptionRequest req =
									SubscriptionRequest
											.newBuilder()
											.setType(
													SubscriptionRequest.Type.BY_SYMBOL)
											.setSymbol("DXH14").build();

							try {

								final ByteArrayOutputStream bos =
										new ByteArrayOutputStream();

								req.writeDelimitedTo(bos);

								final ByteBuf buf = Unpooled.buffer();
								buf.writeShort(BarchartMessageType.SUBSCRIPTION_REQUEST_VALUE);
								buf.writeBytes(bos.toByteArray());
								buf.retain();

								client.send(buf);

							} catch (final Throwable t) {

								t.printStackTrace();

							}

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
