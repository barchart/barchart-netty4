package com.barchart.netty.client.base;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelStateHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

import com.barchart.netty.client.Connectable;
import com.barchart.netty.client.PipelineInitializer;
import com.barchart.netty.client.policy.ReconnectPolicy;
import com.barchart.netty.client.transport.TransportFactory;
import com.barchart.netty.client.transport.TransportProtocol;

/**
 * A base Connectable implementation which provides basic configuration,
 * connection workflow, status monitoring, and message subscriptions.
 */
public abstract class ConnectableBase<T extends Connectable<T>> implements
		Connectable<T>, PipelineInitializer {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected abstract static class Builder<B extends Builder<B, C>, C extends ConnectableBase<C>> {

		/* Standard fields */
		protected TransportProtocol transport;
		protected EventLoopGroup eventLoop = new NioEventLoopGroup();

		/* Implementation specific */
		protected long timeout = 0;

		/**
		 * Set the remote host address to connect to.
		 * 
		 * @see com.barchart.netty.client.transport.TransportFactory#create(URI)
		 */
		@SuppressWarnings("unchecked")
		public B host(final String url) {
			transport = TransportFactory.create(url);
			return (B) this;
		}

		/**
		 * Set the connection read timeout. If the specified time elapses
		 * between inbound messages, the connection will terminate. To
		 * automatically reconnect after a timeout, set a
		 * {@link ReconnectPolicy}.
		 */
		@SuppressWarnings("unchecked")
		public B timeout(final long timeout_, final TimeUnit unit_) {
			timeout = TimeUnit.MILLISECONDS.convert(timeout_, unit_);
			return (B) this;
		}

		/**
		 * Set the Netty EventLoopGroup for this Connectable.
		 */
		@SuppressWarnings("unchecked")
		public B eventLoop(final EventLoopGroup group_) {
			eventLoop = group_;
			return (B) this;
		}

		/**
		 * Retrieve the EventLoopGroup for this connectable. Useful for
		 * scheduling I/O related tasks on the event loop executor.
		 */
		public EventLoopGroup eventLoop() {
			return eventLoop;
		}

		protected C configure(final C client) {
			client.timeout(timeout);
			return client;
		}

		/**
		 * Build a new Connectable client with the current configuration.
		 */
		protected abstract C build();

	}

	/* Message subscriptions */
	private final ConcurrentMap<Class<?>, MessageSubscription<?>> subscriptions =
			new ConcurrentHashMap<Class<?>, MessageSubscription<?>>();

	/* Connection state */
	private final PublishSubject<Connectable.StateChange<T>> stateChanges =
			PublishSubject.create();
	private Connectable.State lastState = null;

	/* Netty resources */
	protected Channel channel;
	private final EventLoopGroup group;
	private final TransportProtocol transport;
	private final ChannelInitializer<Channel> channelInitializer;

	/* Timeout / reconnect */
	private long timeout = 0;

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
			final TransportProtocol transport_) {

		group = eventLoop_;
		transport = transport_;

		channelInitializer = new ClientPipelineInitializer();

		stateChanges.subscribe(new ConnectionStateObserver());

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

		if (transport == null) {
			throw new IllegalArgumentException("Transport cannot be null");
		}

		if (channelInitializer == null) {
			throw new IllegalArgumentException(
					"Channel initializer cannot be null");
		}

		log.debug("Client connecting to " + transport.address().toString());
		changeState(Connectable.State.CONNECTING);

		final Bootstrap bootstrap =
				new Bootstrap().channel(transport.channel()).group(group)
						.remoteAddress(transport.address())
						.option(ChannelOption.SO_REUSEADDR, true)
						.option(ChannelOption.SO_SNDBUF, 262144)
						.option(ChannelOption.SO_RCVBUF, 262144)
						.handler(new ClientPipelineInitializer());

		final ChannelFuture future = bootstrap.connect();

		channel = future.channel();

		final ReplaySubject<T> connectObs = ReplaySubject.create();

		future.addListener(new ChannelFutureListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void operationComplete(final ChannelFuture future)
					throws Exception {

				if (!future.isSuccess()) {
					changeState(Connectable.State.CONNECT_FAIL);
					connectObs.onError(future.cause());
				} else {
					connectObs.onNext((T) ConnectableBase.this);
					connectObs.onCompleted();
				}

			}

		});

		return connectObs;

	}

	@SuppressWarnings("unchecked")
	@Override
	public Observable<T> disconnect() {

		if (channel.isActive()) {

			changeState(Connectable.State.DISCONNECTING);

			return ChannelFutureObservable.create(channel.close(), (T) this);

		}

		return Observable.<T> just((T) this);

	}

	@Override
	public Observable<Connectable.StateChange<T>> stateChanges() {
		return stateChanges;
	}

	@Override
	public Connectable.State state() {
		return lastState;
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

		return ChannelFutureObservable.create(channel.flush(), message);

	}

	/**
	 * Receive messages of a specific type from the connected peer.
	 * 
	 * The message type must be supported by the internal Netty pipeline.
	 * Channel handlers to decode different message types should be provided by
	 * the subclass by overriding the initPipeline() method, otherwise the only
	 * message type available will be ByteBuf.class.
	 * 
	 * This method is not thread-safe. It if is called at the same time as a
	 * connect() attempt the message handler may fail to register.
	 * 
	 * @param type The message type
	 */
	@SuppressWarnings("unchecked")
	protected <U> Observable<U> receive(final Class<U> type) {

		MessageSubscription<U> subscription =
				(MessageSubscription<U>) subscriptions.get(type);

		if (subscription == null) {

			subscription = new MessageSubscription<U>(type);

			final MessageSubscription<?> existing =
					subscriptions.putIfAbsent(type, subscription);

			if (existing != null) {
				subscription = (MessageSubscription<U>) existing;
			}

		}

		return subscription.observable();

	}

	protected final void changeState(final Connectable.State state) {

		final Connectable.State previous = lastState;

		stateChanges.onNext(new Connectable.StateChange<T>() {

			@SuppressWarnings("unchecked")
			@Override
			public T connectable() {
				return (T) ConnectableBase.this;
			}

			@Override
			public Connectable.State state() {
				return state;
			}

			@Override
			public Connectable.State previous() {
				return previous;
			}

		});

	}

	private class ConnectionStateHandler extends ChannelStateHandlerAdapter {

		@Override
		public void inboundBufferUpdated(final ChannelHandlerContext ctx) {
			ctx.fireInboundBufferUpdated();
		}

		@Override
		public void channelActive(final ChannelHandlerContext ctx)
				throws Exception {

			changeState(Connectable.State.CONNECTED);

			super.channelActive(ctx);

		}

		@Override
		public void channelInactive(final ChannelHandlerContext ctx)
				throws Exception {

			changeState(Connectable.State.DISCONNECTED);

			super.channelInactive(ctx);

			channel = null;

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

				// No activity from peer
				changeState(Connectable.State.TIMEOUT);
				ctx.close();

			} else {

				log.warn(cause.getClass().getName() + ": " + cause.getMessage());
				// ctx.fireExceptionCaught(cause);

			}

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
				pipeline.addFirst(new ReadTimeoutHandler(timeout,
						TimeUnit.MILLISECONDS));
			}

			// Monitor connection state
			pipeline.addLast(new ConnectionStateHandler());

			// Process messages and route to observers
			pipeline.addLast(new MessageRouter(subscriptions.values()));

		}

	}

	protected static class ChannelFutureObservable {

		public static <T> Observable<T> create(final ChannelFuture future,
				final T result) {

			final ReplaySubject<T> subject = ReplaySubject.create();

			future.addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(final ChannelFuture future)
						throws Exception {

					if (!future.isSuccess()) {
						subject.onError(future.cause());
					} else {
						subject.onNext(result);
						subject.onCompleted();
					}

				}

			});

			return subject;

		}

	}

	private static class MessageRouter extends
			ChannelInboundMessageHandlerAdapter<Object> {

		private final Collection<MessageSubscription<?>> subscriptions;

		public MessageRouter(
				final Collection<MessageSubscription<?>> subscriptions_) {
			super(Object.class);
			subscriptions = subscriptions_;
		}

		@Override
		public void messageReceived(final ChannelHandlerContext ctx,
				final Object msg) throws Exception {

			for (final MessageSubscription<?> subscription : subscriptions) {
				subscription.route(msg);
			}

		}

	}

	private static class MessageSubscription<M> {

		private final Class<M> type;
		private final PublishSubject<M> publish;

		public MessageSubscription(final Class<M> type_) {
			type = type_;
			publish = PublishSubject.create();
		}

		public Observable<M> observable() {
			return publish;
		}

		public void route(final Object msg) throws Exception {
			if (type.isInstance(msg)) {
				publish.onNext(type.cast(msg));
			}
		}

	}

	private class ConnectionStateObserver implements
			Observer<Connectable.StateChange<T>> {

		@Override
		public void onNext(final Connectable.StateChange<T> state) {
			lastState = state.state();
		}

		@Override
		public void onCompleted() {
		}

		@Override
		public void onError(final Throwable e) {
		}

	}

}
