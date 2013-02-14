package com.barchart.netty.host.impl;

import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.host.api.NettyPipe;
import com.barchart.netty.host.api.NettyPipeManager;
import com.barchart.util.collections.BlockingConcurrentHashMap;

/** pipeline collector */
@Component(immediate = true)
public class NettyPipeProvider implements NettyPipeManager {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final BlockingConcurrentHashMap<String, NettyPipe> pipeMap = //
			new BlockingConcurrentHashMap<String, NettyPipe>();

	@Override
	public NettyPipe findPipe(final String pipeName) {
		if (pipeName == null) {
			return null;
		}
		return pipeMap.get(pipeName);
	}

	@Override
	public NettyPipe findPipe(final String pipeName, final long timeout,
			final TimeUnit unit) throws InterruptedException {
		if (pipeName == null) {
			return null;
		}
		return pipeMap.get(pipeName, timeout, unit);
	}

	@Reference( //
	policy = ReferencePolicy.DYNAMIC, //
	cardinality = ReferenceCardinality.MULTIPLE //
	)
	protected void bind(final NettyPipe pipe) {

		pipeMap.put(pipe.type(), pipe);

		log.debug("@@@ pipe-bind : {}", pipe.type());

	}

	protected void unbind(final NettyPipe pipe) {

		pipeMap.remove(pipe.type());

		log.debug("@@@ pipe-unbind : {}", pipe.type());

	}

}
