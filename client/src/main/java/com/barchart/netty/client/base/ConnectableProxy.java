package com.barchart.netty.client.base;

import io.netty.channel.ChannelPipeline;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.barchart.netty.client.Connectable;
import com.barchart.netty.client.facets.ConnectableFacet;
import com.barchart.netty.client.transport.TransportProtocol;
import com.barchart.netty.common.PipelineInitializer;

/**
 * Connectable proxy generator for pluggable combinations of client features.
 * For read-heavy environments, using a proxy client has no impact on throughput
 * performance, as only the initial handler registration goes through the proxy.
 * 
 * Proxies are constructed by adding feature "facets" represented by a
 * ConnectableFacet.
 * 
 * <pre>
 * ExampleProxyClient client = new ExampleProxyClient("tcp://localhost:6497");
 *
 * // Add dynamic facets
 * client.facet(new SecureFacet(SecureFacet.Encryption.REFUSE));
 * client.facet(new KeepaliveFacet(10, TimeUnit.SECONDS));
 * 
 * client.connect();
 * </pre>
 */
public abstract class ConnectableProxy<T extends ConnectableProxy<T>> extends
		ConnectableBase<T> implements InvocationHandler {

	private final List<ConnectableFacet<?>> facets =
			new ArrayList<ConnectableFacet<?>>();

	private final List<Object> invokers = new ArrayList<Object>();

	private final ConcurrentMap<Method, Object> methods =
			new ConcurrentHashMap<Method, Object>();

	protected ConnectableProxy(final TransportProtocol transport_) {

		super(transport_);

		invokers.add(this);

	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {

		for (final PipelineInitializer p : facets) {
			p.initPipeline(pipeline);
		}

	}

	@SuppressWarnings("unchecked")
	public <P> T facet(final ConnectableFacet<P> facet) {

		if (!facet.type().isInstance(facet)) {
			throw new ClassCastException(
					"Facet handler must be an instance of its type()");
		}

		facets.add(facet);
		invokers.add(facet);

		return (T) this;

	}

	/**
	 * Create a new proxy instance for this connectable.
	 */
	@SuppressWarnings("unchecked")
	public <U extends Connectable<U>> U proxy(final Class<U> type) {

		final ArrayList<Class<?>> types = new ArrayList<Class<?>>();
		types.add(type);
		for (final ConnectableFacet<?> f : facets) {
			types.add(f.type());
		}

		return (U) Proxy.newProxyInstance(
				ConnectableProxy.class.getClassLoader(),
				types.toArray(new Class<?>[] {}), this);

	}

	@Override
	public Object invoke(final Object proxy, final Method method,
			final Object[] args) throws Throwable {

		// Check cache
		if (methods.containsKey(method)) {
			return method.invoke(methods.get(method), args);
		}

		// Check for exact match
		for (final ConnectableFacet<?> f : facets) {
			if (method.getDeclaringClass() == f.type()) {
				methods.put(method, f);
				return method.invoke(f, args);
			}
		}

		// Check for instance
		for (final Object o : invokers) {
			if (method.getDeclaringClass().isInstance(o)) {
				methods.put(method, o);
				return method.invoke(o, args);
			}
		}

		// Nothing found
		throw new NoSuchMethodException();

	}
}
