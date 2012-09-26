package com.barchart.netty.part.pipe;

import io.netty.channel.Channel;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.host.api.NettyDot;

/** empty pipe */
@Component(name = PipeInvalid.NAME, immediate = true)
public class PipeInvalid extends PipeAny {

	public static final String NAME = "barchart.netty.pipe.invalid";

	@Override
	public String componentName() {
		return NAME;
	}

	@Override
	protected void applyDefault(final NettyDot dot, final Channel channel) {

		log.debug("apply default : {}", channel);

	}

	@Override
	protected void applyDerived(final NettyDot dot, final Channel channel) {

		log.debug("apply derived : {}", channel);

	}

}
