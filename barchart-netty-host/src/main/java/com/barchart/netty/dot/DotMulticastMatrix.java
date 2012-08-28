package com.barchart.netty.dot;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.barchart.netty.matrix.api.Matrix;
import com.barchart.osgi.factory.api.FactoryDescriptor;

@Component(factory = DotMulticastMatrix.FACTORY)
public class DotMulticastMatrix extends DotMulticast {

	public static final String FACTORY = "barchart.netty.dot.multicast.matrix";

	@FactoryDescriptor
	private static final Map<String, String> descriptor;
	static {
		descriptor = new HashMap<String, String>();
		descriptor.put(PROP_FACTORY_ID, FACTORY);
		descriptor.put(PROP_FACTORY_DESCRIPTION,
				"multicast reader end point service with matix handler");
	}

	@Override
	protected ChannelInitializer<DatagramChannel> handler() {
		return new ChannelInitializer<DatagramChannel>() {
			@Override
			public void initChannel(final DatagramChannel ch) throws Exception {
				ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
			}
		};
	}

	private Matrix matrix;

	@Reference
	protected void bind(final Matrix s) {
		matrix = s;
	}

	protected void unbind(final Matrix s) {
		matrix = null;
	}

}
