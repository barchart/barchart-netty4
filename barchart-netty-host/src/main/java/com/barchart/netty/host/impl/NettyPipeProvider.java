package com.barchart.netty.host.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.host.api.NettyPipe;
import com.barchart.netty.host.api.NettyPipeManager;

/** pipeline collector */
@Component(immediate = true)
public class NettyPipeProvider implements NettyPipeManager {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final ConcurrentMap<String, NettyPipe> pipeMap = //
	new ConcurrentHashMap<String, NettyPipe>();

	@Override
	public NettyPipe findPipe(final String pipeName) {
		if (pipeName == null) {
			return null;
		}
		return pipeMap.get(pipeName);
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
