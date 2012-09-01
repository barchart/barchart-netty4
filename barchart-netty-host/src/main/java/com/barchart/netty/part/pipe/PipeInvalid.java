package com.barchart.netty.part.pipe;

import io.netty.channel.Channel;

import org.osgi.service.component.annotations.Component;

/** empty pipe */
@Component(name = PipeInvalid.NAME, immediate = true)
public class PipeInvalid extends PipeAny {

	public static final String NAME = "barchart.netty.pipe.invalid";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void apply(final Channel channel) {

	}

	@Override
	public void applyChild(final Channel channel) {

	}

}
