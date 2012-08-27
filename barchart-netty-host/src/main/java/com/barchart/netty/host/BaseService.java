package com.barchart.netty.host;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.part.BasePart;
import com.barchart.netty.util.point.NetPoint;

public abstract class BaseService extends BasePart {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	//

	private Channel channel;

	private ChannelFuture futureClose;

	private ChannelFuture futureConnect;

	//

	private NetPoint netPoint;

	// private ChannelFactory channelFactory;

	// private NetPipelineFactory piplineFactory;

	//

	// @Reference
	// protected void bind(final ChannelFactory factory) {
	// channelFactory = factory;
	// }

	// protected void unbind(final ChannelFactory factory) {
	// channelFactory = null;
	// }

	// @Reference
	// protected void bind(final NetPipelineFactory factory) {
	// piplineFactory = factory;
	// }

	// protected void unbind(final NetPipelineFactory factory) {
	// piplineFactory = null;
	// }

	@Reference
	protected void bind(final NetPoint point) {
		netPoint = point;
	}

	protected void unbind(final NetPoint point) {
		netPoint = null;
	}

	public Channel getChannel() {
		return channel;
	}

	public ChannelFuture getFutureClose() {
		return futureClose;
	}

	public ChannelFuture getFutureConnect() {
		return futureConnect;
	}

	public NetPoint getNetPoint() {
		return netPoint;
	}

	// public ChannelFactory getChannelFactory() {
	// return channelFactory;
	// }

	// public NetPipelineFactory getNetPipelineFactory() {
	// return piplineFactory;
	// }

	protected void setChannel(final Channel channel) {
		this.channel = channel;
	}

	protected void setFutureClose(final ChannelFuture futureClose) {
		this.futureClose = futureClose;
	}

	protected void setFutureConnect(final ChannelFuture futureConnect) {
		this.futureConnect = futureConnect;
	}

	@Override
	public synchronized void start() {

		super.start();

	}

	@Override
	public synchronized void stop() {

		super.stop();

	}

	public abstract ChannelFuture write(Object message);

}
