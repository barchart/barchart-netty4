package com.barchart.netty.host.impl;

import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.logging.InternalLoggerFactory;
import io.netty.logging.Slf4JLoggerFactory;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.dot.BaseServiceProvider;
import com.barchart.netty.host.api.NettyHost;
import com.barchart.netty.host.api.NettyService;
import com.barchart.netty.util.point.NetPoint;
import com.barchart.osgi.factory.api.FidgetManagerBase;

/** TODO enable SCTP */

@Component
public class NettyHostProvider extends FidgetManagerBase<NettyService>
		implements NettyHost {

	static {
		/** use slf4j for internal netty LoggingHandler */
		final InternalLoggerFactory defaultFactory = new Slf4JLoggerFactory();
		InternalLoggerFactory.setDefaultFactory(defaultFactory);
	}

	protected final Logger log = LoggerFactory.getLogger(getClass());

	//

	private final ConcurrentMap<NetPoint, BaseServiceProvider> serviceMap = //
	new ConcurrentHashMap<NetPoint, BaseServiceProvider>();

	public BaseServiceProvider getService(final NetPoint point) {
		return serviceMap.get(point);
	}

	//

	private final Timer timer = new HashedWheelTimer();

	public Timer getTimer() {
		return timer;
	}

	//

	private NioEventLoopGroup poolServer;
	private NioEventLoopGroup poolClient;

	public void bindAllExecutorPools(final Executor executor,
			final int countServer, //
			final int countClient //
	) {

	}

	//

	public BaseServiceProvider makeService(final NetPoint point) {

		if (point == null) {
			log.error("point", new Exception("unexpected"));
			return null;
		}

		final BaseServiceProvider service = null;

		// final ChannelFactory channelFactory;
		//
		// switch (pipelineFactory.getRole()) {
		//
		// case MULTICAST_READER:
		// case MULTICAST_WRITER:
		// case UNICAST_CONDUIT:
		// service = new PacketConduit();
		// channelFactory = newPacketConduitCF();
		// break;
		//
		// case STREAM_CLIENT:
		// service = new StreamClient();
		// channelFactory = newStreamClientCF();
		// break;
		//
		// case STREAM_SERVER:
		// service = new StreamServer();
		// channelFactory = newStreamServerCF();
		// break;

		// case CONVOY_CLIENT:
		// service = new ConvoyClient();
		// channelFactory = newConvoyClientCF();
		// break;

		// case CONVOY_SERVER:
		// service = new ConvoyServer();
		// channelFactory = newConvoyServerCF();
		// break;

		// default:
		// log.error("role", new Exception("unexpected"));
		// return null;

		// }

		// service.bind(point);

		// service.bind(pipelineFactory);

		// serviceMap.put(point, service);

		return service;

	}

	//

	public void activate() {

		for (final BaseServiceProvider service : serviceMap.values()) {
			service.activate();
		}

	}

	public void deactivate() {

		for (final BaseServiceProvider service : serviceMap.values()) {
			service.deactivate();
		}

	}

	@Override
	protected Class<NettyService> getFidgetInterface() {
		return NettyService.class;
	}

}
