package com.barchart.netty.host;

import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.logging.InternalLoggerFactory;
import io.netty.logging.Slf4JLoggerFactory;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

import com.barchart.netty.part.BasePart;
import com.barchart.netty.util.point.NetPoint;

/** TODO enable SCTP */
public class BaseHost extends BasePart {

	static {
		/** use slf4j for internal netty LoggingHandler */
		final InternalLoggerFactory defaultFactory = new Slf4JLoggerFactory();
		InternalLoggerFactory.setDefaultFactory(defaultFactory);
	}

	//

	private final ConcurrentMap<NetPoint, BaseService> serviceMap = //
	new ConcurrentHashMap<NetPoint, BaseService>();

	public BaseService getService(final NetPoint point) {
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

	public BaseService makeService(final NetPoint point) {

		if (point == null) {
			log.error("point", new Exception("unexpected"));
			return null;
		}

		final BaseService service = null;

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

		service.bind(point);

		// service.bind(pipelineFactory);

		// serviceMap.put(point, service);

		return service;

	}

	//

	@Override
	public synchronized void start() {

		super.start();

		for (final BaseService service : serviceMap.values()) {
			service.start();
		}

	}

	@Override
	public synchronized void stop() {

		for (final BaseService service : serviceMap.values()) {
			service.stop();
		}

		super.stop();

	}

}
