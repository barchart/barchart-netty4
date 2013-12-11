package com.barchart.netty.client;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import rx.Observable;

import com.barchart.netty.client.transport.TransportProtocol;

public interface Connectable<T extends Connectable<T>> {

	public enum State {

		/**
		 * The client is about to connect.
		 */
		CONNECTING,

		/**
		 * Connection succeeded.
		 */
		CONNECTED,

		/**
		 * Connection attempt failed.
		 */
		CONNECT_FAIL,

		/**
		 * Client disconnected due to a read timeout.
		 */
		TIMEOUT,

		/**
		 * The client is about to disconnect.
		 */
		DISCONNECTING,

		/**
		 * The client successfully disconnected.
		 */
		DISCONNECTED

	}

	public interface Builder<C extends Connectable<C>> {

		/**
		 * The remote peer's address.
		 * 
		 * Either address() or websocket() should be called but not both.
		 */
		Builder<? extends C> address(InetSocketAddress address,
				TransportProtocol protocol);

		/**
		 * Use a websocket connection. Automatically sets the peer address based
		 * on the URI and connects over TCP.
		 * 
		 * Either address() or websocket() should be called but not both.
		 */
		Builder<? extends C> websocket(URI uri);

		/**
		 * Set the automatic reconnect delay after an unexpected disconnect or
		 * read timeout. Disabled by default.
		 */
		Builder<? extends C> reconnect(long delay, TimeUnit unit);

		/**
		 * Set the read timeout for this connection. If a message has not been
		 * received in the specified number of milliseconds, the connection will
		 * be terminated (automatically triggering a reconnect attempt if
		 * reconnect() is set). Disabled by default.
		 */
		Builder<? extends C> timeout(long timeout, TimeUnit unit);

		/**
		 * Build a Connectable instance with the current parameters.
		 */
		C build();

	}

	/**
	 * Connect to the remote peer.
	 */
	Observable<T> connect();

	/**
	 * Disconnect from peer.
	 */
	Observable<T> disconnect();

	/**
	 * Observe connection state changes.
	 */
	Observable<State> stateChanges();

	/**
	 * Check the last connection state.
	 */
	State state();

}
