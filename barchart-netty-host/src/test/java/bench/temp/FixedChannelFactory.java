package bench.temp;

import io.netty.bootstrap.AbstractBootstrap.ChannelFactory;
import io.netty.channel.Channel;

/**
 * https://github.com/netty/netty/issues/609
 */
public class FixedChannelFactory implements ChannelFactory {

	private final Channel channel;

	public FixedChannelFactory(final Channel channel) {
		this.channel = channel;
	}

	@Override
	public Channel newChannel() {
		return channel;
	}

}
