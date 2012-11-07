package com.barchart.netty.part.boot;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.NetworkConstants;

import java.net.InetAddress;
import java.net.NetworkInterface;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.host.api.NettyPipe;
import com.barchart.netty.host.impl.OperatingSystem;
import com.barchart.netty.util.point.NetAddress;
import com.barchart.netty.util.point.NetPoint;

/**
 * parent for multicast end points
 * 
 * handles multicast join / leave;
 */
@Component(name = BootCastMulti.TYPE, immediate = true)
public class BootCastMulti extends BootCast {

	public static final String TYPE = "barchart.netty.boot.cast.multi";

	@Override
	public String type() {
		return TYPE;
	}

	/** valid interface or loop back interface for error */
	protected NetworkInterface bindInterface(final NetPoint netPoint) {
		try {

			final InetAddress address = netPoint.getLocalAddress().getAddress();

			final NetworkInterface iface = NetworkInterface
					.getByInetAddress(address);

			if (iface == null) {
				throw new IllegalArgumentException(
						"address is not assigned to any iterface : " + address);
			}

			return iface;

		} catch (final Throwable e) {
			log.error("fatal: can not resolve bind interface", e);
			return NetworkConstants.LOOPBACK_IF;
		}
	}

	/**
	 * multicast reader bind address: listen on any local address with a
	 * multicast group port
	 */
	protected NetAddress localAddress(final NetPoint netPoint) {
		return new NetAddress("0.0.0.0", groupAddress(netPoint).getPort());
	}

	/** multicast reader group address : */
	protected NetAddress groupAddress(final NetPoint netPoint) {
		return netPoint.getRemoteAddress();
	}

	/** valid bind address or local host for error */
	/** FIXME slow dns lookup */
	protected NetAddress bindAddress(final NetPoint netPoint) {
		try {
			/**
			 * allows to avoid duplicate packets when multiple multicast groups
			 * share the same port
			 */
			final InetAddress bindAddr;
			final int bindPort = groupAddress(netPoint).getPort();
			switch (OperatingSystem.CURRENT) {
			case LINUX:
				bindAddr = groupAddress(netPoint).getAddress();
				break;
			case WINDOWS:
				bindAddr = localAddress(netPoint).getAddress();
				break;
			default:
				log.error("", new Exception(
						"possible bind problem - o/s not tested"));
				bindAddr = InetAddress.getByName("0.0.0.0");
				break;
			}
			return new NetAddress(bindAddr, bindPort);
		} catch (final Throwable e) {
			log.error("fatal: can not resolve bind address", e);
			return new NetAddress(NetworkConstants.LOCALHOST, 0);
		}
	}

	@Override
	public ChannelFuture startup(final NetPoint netPoint) throws Exception {

		final NioDatagramChannel channel = (NioDatagramChannel) new Bootstrap()
		.localAddress(netPoint.getRemoteAddress().getPort())
		.remoteAddress(netPoint.getRemoteAddress())
		.channel(NioDatagramChannel.class)
		.option(ChannelOption.SO_SNDBUF, netPoint.getSendBufferSize())
		.option(ChannelOption.SO_RCVBUF, netPoint.getReceiveBufferSize())
		.option(ChannelOption.SO_REUSEADDR, true)
		.option(ChannelOption.IP_MULTICAST_TTL, netPoint.getPacketTTL())
		.group(group())
		.handler(pipeApply(netPoint, NettyPipe.Mode.DEFAULT))
		.bind()
		.sync()
		.channel();
		
	return channel.joinGroup(groupAddress(netPoint),
			bindInterface(netPoint)).sync();

	}

	@Override
	public ChannelFuture shutdown(final NetPoint netPoint, final Channel channel)
			throws Exception {

		((NioDatagramChannel) channel).leaveGroup(groupAddress(netPoint),
				bindInterface(netPoint)).sync();

		return super.shutdown(netPoint, channel);

	}

}
