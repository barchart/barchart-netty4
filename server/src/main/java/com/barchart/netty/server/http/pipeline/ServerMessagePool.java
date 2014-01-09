package com.barchart.netty.server.http.pipeline;

import java.util.concurrent.Callable;

import com.barchart.netty.server.util.ObjectPool;

/**
 * HTTP request/response object pool for low-garbage request handling
 */
public class ServerMessagePool {

	private final ObjectPool<PooledServerRequest> requestPool;
	private final ObjectPool<PooledServerResponse> responsePool;

	/**
	 * Create a new fixed-size message pool.
	 * 
	 * @param maxObjects_
	 *            The pool size, or -1 for unlimited
	 */
	public ServerMessagePool(final int maxObjects_) {

		requestPool =
				new ObjectPool<PooledServerRequest>(maxObjects_,
						new Callable<PooledServerRequest>() {
							@Override
							public PooledServerRequest call() throws Exception {
								return new PooledServerRequest();
							}
						});

		responsePool =
				new ObjectPool<PooledServerResponse>(maxObjects_,
						new Callable<PooledServerResponse>() {
							@Override
							public PooledServerResponse call() throws Exception {
								return new PooledServerResponse(
										ServerMessagePool.this);
							}
						});

	}

	/**
	 * Get an available request object, or null if none are available.
	 * 
	 * @return A pooled request object
	 */
	public PooledServerRequest getRequest() {
		return requestPool.poll();
	}

	/**
	 * Get an available response object, or null if none are available.
	 * 
	 * @return A pooled response object
	 */
	public PooledServerResponse getResponse() {
		return responsePool.poll();
	}

	void makeAvailable(final PooledServerRequest request) {
		requestPool.give(request);
	}

	void makeAvailable(final PooledServerResponse response) {
		responsePool.give(response);
	}
}
