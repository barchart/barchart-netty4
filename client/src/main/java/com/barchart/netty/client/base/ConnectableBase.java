package com.barchart.netty.client.base;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundMessageHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observer;
import rx.Subscription;

import com.barchart.netty.client.Connectable;
import com.barchart.netty.client.PipelineInitializer;
import com.barchart.netty.client.transport.PassthroughStateHandler;
import com.barchart.netty.client.transport.TransportProtocol;
import com.barchart.netty.client.transport.WebSocketTransport;

public abstract class ConnectableBase<T extends ConnectableBase<T>> implements
		Connectable<T>, PipelineInitializer {

	private final Logger log = LoggerFactory.getLogger(getClass());

	protected abstract static class Builder<B extends Builder<B, C>, C extends ConnectableBase<C>>
			implements Connectable.Builder<C> {

		/* Standard fields */
		protected InetSocketAddress address;
		protected TransportProtocol transport;
		protected EventLoopGroup eventLoop = new NioEventLoopGroup();

		/* Implementation specific */
		protected long reconnect = -1;
		protected long timeout = 0;

		@SuppressWarnings("unchecked")
		@Override
		public B address(final InetSocketAddress address_,
				final TransportProtocol transport_) {

			address = address_;
			transport = transport_;

			return (B) this;

		}

		@SuppressWarnings("unchecked")
		@Override
		public B websocket(final URI uri) {

			int port = uri.getPort();

			if (port == -1) {
				port = uri.getScheme().equalsIgnoreCase("wss") ? 443 : 80;
			}

			address = InetSocketAddress.createUnresolved(uri.getHost(), port);
			transport = new WebSocketTransport(uri);

			return (B) this;

		}

		@SuppressWarnings("unchecked")
		@Override
		public B reconnect(final long delay_, final TimeUnit unit_) {
			reconnect = TimeUnit.MILLISECONDS.convert(delay_, unit_);
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public B timeout(final long timeout_, final TimeUnit unit_) {
			timeout = TimeUnit.MILLISECONDS.convert(timeout_, unit_);
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B eventLoop(final EventLoopGroup group_) {
			eventLoop = group_;
			return (B) this;
		}

		protected C configure(final C client) {
			client.reconnect(reconnect);
			client.timeout(timeout);
			return client;
		}

	}

	/* Message subscriptions */
	private final ConcurrentMap<Class<?>, MessageSubscription<?>> subscriptions =
			new ConcurrentHashMap<Class<?>, MessageSubscription<?>>();

	/* Connection state */
	private final ConnectionStateChange stateChanges =
			new ConnectionStateChange();

	/* Netty resources */
	protected Channel channel;
	private final EventLoopGroup group;
	private final InetSocketAddress address;
	private final TransportProtocol transport;
	private final ChannelInitializer<Channel> channelInitializer;

	/* Timeout / reconnect */
	private long timeout = 0;
	private long reconnect = -1;

	/**
	 * Create a new connectable client. This method is intended to be called by
	 * subclass Builder implementations.
	 * 
	 * @param eventLoop_ The Netty EventLoopGroup to use for transport
	 *            operations
	 * @param address_ The remote peer address
	 * @param transport_ The transport type
	 */
	protected ConnectableBase(final EventLoopGroup eventLoop_,
			final InetSocketAddress address_, final TransportProtocol transport_) {

		group = eventLoop_;
		address = address_;
		transport = transport_;

		channelInitializer = new ClientPipelineInitializer();

	}

	/**
	 * The current reconnect delay in milliseconds. -1 indicates no automatic
	 * reconnect.
	 */
	protected long reconnect() {
		return reconnect;
	}

	/**
	 * Set the reconnect delay in milliseconds. Set to -1 to disable reconnect.
	 */
	protected void reconnect(final long millis) {
		reconnect = millis;
	}

	/**
	 * The current read timeout in milliseconds. 0 indicates no timeout.
	 */
	protected long timeout() {
		return timeout;
	}

	/**
	 * Set the read timeout in milliseconds. Set to 0 to disable timeout.
	 */
	protected void timeout(final long millis) {
		timeout = millis;
	}

	@Override
	public Observable<T> connect() {

		if (address == null) {
			throw new IllegalArgumentException("Peer address cannot be null");
		}

		if (transport == null) {
			throw new IllegalArgumentException("Transport cannot be null");
		}

		if (channelInitializer == null) {
			throw new IllegalArgumentException(
					"Channel initializer cannot be null");
		}

		log.debug("Client connecting to " + address.toString());
		stateChanges.fire(Connectable.State.CONNECTING);

		final ChannelFuture future =
				new Bootstrap().channel(transport.channel()).group(group)
						.remoteAddress(address)
						.option(ChannelOption.SO_REUSEADDR, true)
						.option(ChannelOption.SO_SNDBUF, 262144)
						.option(ChannelOption.SO_RCVBUF, 262144)
						.handler(new ClientPipelineInitializer()).connect();

		channel = future.channel();

		final PushSubscription<T> connectObs = new PushSubscription<T>();

		future.addListener(new ChannelFutureListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void operationComplete(final ChannelFuture future)
					throws Exception {

				if (!future.isSuccess()) {

					stateChanges.fire(Connectable.State.CONNECT_FAIL);

					if (reconnect > -1) {

						future.channel().eventLoop().schedule(new Runnable() {
							@Override
							public void run() {
								log.debug("Connect failed, reconnecting");
								connect();
							}
						}, reconnect, TimeUnit.MILLISECONDS);

					} else {
						connectObs.error(future.cause());
					}

				} else {
					connectObs.push((T) ConnectableBase.this);
					connectObs.complete();
				}

			}

		});

		return Observable.create(connectObs).cache();

	}

	@SuppressWarnings("unchecked")
	@Override
	public Observable<T> disconnect() {

		stateChanges.fire(Connectable.State.DISCONNECTING);

		reconnect = -1;

		return Observable.create(new ChannelFutureSubscription<T>(channel
				.close(), (T) this));

	}

	@Override
	public Observable<Connectable.State> stateChanges() {
		return Observable.create(stateChanges);
	}

	@Override
	public Connectable.State state() {
		return stateChanges.last();
	}

	/**
	 * Send a message to the connected peer. The message type must be supported
	 * by the internal Netty pipeline.
	 * 
	 * @param message An object to encode and send to the remote peer
	 */
	protected <U> Observable<U> send(final U message) {

		if (!channel.isActive()) {
			throw new IllegalStateException("Channel is not active");
		}

		channel.write(message);

		return Observable.create(new ChannelFutureSubscription<U>(channel
				.flush(), message));

	}

	/**
	 * Receive messages of a specific type from the connected peer.
	 * 
	 * The message type must be supported by the internal Netty pipeline.
	 * Channel handlers to decode different message types should be provided by
	 * the subclass by overriding the initPipeline() method, otherwise the only
	 * message type available will be ByteBuf.class.
	 * 
	 * @param type The message type
	 */
	@SuppressWarnings("unchecked")
	protected <U> Observable<U> receive(final Class<U> type) {

		MessageSubscription<U> subscription =
				(MessageSubscription<U>) subscriptions.get(type);

		if (subscription == null) {

			subscription = new MessageSubscription<U>();

			final MessageSubscription<?> existing =
					subscriptions.putIfAbsent(type, subscription);

			if (existing != null) {
				subscription = (MessageSubscription<U>) existing;
			}

		}

		return Observable.create(subscription);

	}

	private class ClientChannelHandler extends ChannelDuplexHandler implements
			ChannelInboundMessageHandler<Object>,
			ChannelOutboundMessageHandler<Object> {

		@Override
		public void inboundBufferUpdated(final ChannelHandlerContext ctx) {

			final Queue<Object> messages = ctx.inboundMessageBuffer();

			final MessageBuf<Object> nextBuffer =
					ctx.nextInboundMessageBuffer();

			while (messages.size() > 0) {

				try {

					final Object message = messages.poll();

					if (nextBuffer != null) {

						final MessageSubscription<?> subscription =
								subscriptions.get(message.getClass());

						if (subscription != null) {
							subscription.route(message);
						} else {
							nextBuffer.add(message);
						}

					}

				} catch (final Exception e) {
					log.warn("Exception processing inbound messages", e);
				}

			}

			if (nextBuffer != null && nextBuffer.size() > 0) {
				ctx.fireInboundBufferUpdated();
			}

		}

		@Override
		public void flush(final ChannelHandlerContext ctx,
				final ChannelPromise promise) {

			final Queue<Object> messages = ctx.outboundMessageBuffer();

			final MessageBuf<Object> nextBuffer =
					ctx.nextOutboundMessageBuffer();

			Object message;
			while ((message = messages.poll()) != null) {
				nextBuffer.add(message);
			}

			ctx.flush(promise);

		}

		@Override
		public void channelInactive(final ChannelHandlerContext ctx)
				throws Exception {

			stateChanges.fire(Connectable.State.DISCONNECTED);

			if (reconnect > -1) {
				ctx.close().addListener(new ChannelFutureListener() {

					@Override
					public void operationComplete(final ChannelFuture future)
							throws Exception {

						if (!future.isSuccess()) {

							if (reconnect > -1) {

								future.channel().eventLoop()
										.schedule(new Runnable() {
											@Override
											public void run() {
												log.debug("Disconnected, reconnecting");
												ConnectableBase.this.connect();
											}
										}, reconnect, TimeUnit.MILLISECONDS);

							} else {
								stateChanges
										.fire(Connectable.State.CONNECT_FAIL);
							}

						}

					}

				});

			}

			super.channelInactive(ctx);

		}

		@Override
		public void exceptionCaught(final ChannelHandlerContext ctx,
				final Throwable cause) {

			if (cause instanceof DecoderException) {

				// Discard inbound buffer on decode error
				final ByteBuf buf = ctx.pipeline().inboundByteBuffer();
				buf.skipBytes(buf.readableBytes());

				log.info("Could not decode message, discarding inbound buffer");
				log.debug("", cause);

			} else if (cause instanceof ReadTimeoutException) {

				// No activity from peer, close to trigger reconnect
				stateChanges.fire(Connectable.State.TIMEOUT);
				ctx.close();

			} else {

				ctx.fireExceptionCaught(cause);

			}

		}

		@Override
		public MessageBuf<Object> newOutboundBuffer(
				final ChannelHandlerContext ctx) throws Exception {
			return Unpooled.messageBuffer();
		}

		@Override
		public MessageBuf<Object> newInboundBuffer(
				final ChannelHandlerContext ctx) throws Exception {
			return Unpooled.messageBuffer();
		}

	}

	private class ClientPipelineInitializer extends ChannelInitializer<Channel> {

		@Override
		public void initChannel(final Channel ch) throws Exception {

			final ChannelPipeline pipeline = ch.pipeline();

			// User-specified pipeline handlers (message codecs)
			initPipeline(pipeline);

			// Transport-required pipeline handlers
			transport.initPipeline(pipeline);

			// Connection read timeout handler
			if (timeout > 0) {
				pipeline.addLast(new ReadTimeoutHandler(timeout,
						TimeUnit.MILLISECONDS));
			}

			// Process messages and route to observers
			pipeline.addLast(new ClientChannelHandler());

			// Listen for connected event from transport, re-fire as a
			// connection state change
			pipeline.addLast(new ConnectedEventNotifier());

		}

	}

	protected static class PushSubscription<T> implements
			Observable.OnSubscribeFunc<T> {

		protected final Logger log = LoggerFactory.getLogger(getClass());

		private final Set<Observer<? super T>> observers =
				new CopyOnWriteArraySet<Observer<? super T>>();

		protected void push(final T item) {

			for (final Observer<? super T> obs : observers) {
				try {
					obs.onNext(item);
				} catch (final Throwable t) {
					log.error("Uncaught exception", t);
				}
			}

		}

		protected void complete() {

			for (final Observer<? super T> obs : observers) {
				try {
					obs.onCompleted();
				} catch (final Throwable t) {
					log.error("Uncaught exception", t);
				}
			}

		}

		protected void error(final Throwable error) {

			for (final Observer<? super T> obs : observers) {
				try {
					obs.onError(error);
				} catch (final Throwable t) {
					log.error("Uncaught exception", t);
				}
			}

		}

		@Override
		public Subscription onSubscribe(final Observer<? super T> observer) {

			observers.add(observer);

			return new Subscription() {
				@Override
				public void unsubscribe() {
					observers.remove(observer);
				}
			};

		}

	}

	protected static class ChannelFutureSubscription<T> extends
			PushSubscription<T> {

		public ChannelFutureSubscription(final ChannelFuture future,
				final T result) {

			future.addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(final ChannelFuture future)
						throws Exception {

					if (!future.isSuccess()) {
						error(future.cause());
					} else {
						push(result);
						complete();
					}

				}

			});

		}

	}

	private static class ConnectionStateChange extends
			PushSubscription<Connectable.State> {

		private Connectable.State lastState;

		protected void fire(final Connectable.State state) {
			log.debug("Connection state fired: " + state);
			lastState = state;
			push(state);
		}

		public Connectable.State last() {
			return lastState;
		}

	}

	private static class MessageSubscription<M> extends PushSubscription<M> {

		@SuppressWarnings("unchecked")
		public void route(final Object message) {
			push((M) message);
		}

	}

	private class ConnectedEventNotifier extends PassthroughStateHandler {

		@Override
		public void userEventTriggered(final ChannelHandlerContext ctx,
				final Object evt) throws Exception {

			if (evt == TransportProtocol.Event.CONNECTED) {
				stateChanges.fire(Connectable.State.CONNECTED);
			} else if (evt == TransportProtocol.Event.DISCONNECTED) {
				stateChanges.fire(Connectable.State.DISCONNECTED);
			}

			super.userEventTriggered(ctx, evt);

		}

	}

}
