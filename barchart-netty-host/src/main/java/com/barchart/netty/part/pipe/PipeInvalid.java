package com.barchart.netty.part.pipe;

import io.netty.channel.Channel;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.host.api.NettyPipe;

/** empty pipe */
@Component(name = PipeInvalid.NAME, immediate = true)
public class PipeInvalid implements NettyPipe {

	public static final String NAME = "barchart.netty.pipe.invalid";

	@Override
	public String getName() {
		return NAME;
	}

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void apply(final Channel channel) {

	}

}
