package com.barchart.netty.client;

import rx.Observable;

public interface Connectable<T extends Connectable<T>> {

	interface StateChange<C extends Connectable<C>> {

		C connectable();

		State state();

		State previous();

	}

	public enum State {

		/**
		 * The client is about to connect.
		 */
		CONNECTING,

		/**
		 * Connection succeeded.
		 * 
		 * This does not represent the underlying channel state, but indicates
		 * that the connection is ready for application use. This is due to some
		 * transports (i.e. websockets, TLS) requiring additional handshaking
		 * after channel activation before the application should consider it
		 * ready.
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
	Observable<StateChange<T>> stateChanges();

	/**
	 * Check the last connection state.
	 */
	State state();

}
