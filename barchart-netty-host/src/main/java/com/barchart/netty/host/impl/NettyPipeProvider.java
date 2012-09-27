package com.barchart.netty.host.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.barchart.netty.host.api.NettyPipe;
import com.barchart.netty.host.api.NettyPipeManager;

/** pipeline collector */
@Component(immediate = true)
public class NettyPipeProvider implements NettyPipeManager {

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

	}

	protected void unbind(final NettyPipe pipe) {

		pipeMap.remove(pipe.type());

	}

}
