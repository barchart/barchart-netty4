package com.barchart.netty.dot;

import io.netty.channel.ChannelOption;
import io.netty.util.NetworkConstants;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.host.impl.OperatingSystem;
import com.barchart.netty.util.point.NetAddress;
import com.barchart.osgi.factory.api.FactoryDescriptor;

@Component(factory = DotMulticast.FACTORY)
public class DotMulticast extends DotUnicast {

	public static final String FACTORY = "barchart.netty.dot.multicast";

	@FactoryDescriptor
	private static final Map<String, String> descriptor;
	static {
		descriptor = new HashMap<String, String>();
		descriptor.put(PROP_FACTORY_ID, FACTORY);
		descriptor.put(PROP_FACTORY_DESCRIPTION,
				"multicast reader end point service");
	}

	/** valid interface or local host */
	protected NetworkInterface getBindInteface() {
		try {
			return NetworkInterface.getByInetAddress(//
					getLocalAddress().getAddress());
		} catch (final Throwable e) {
			log.error("fatal: can not resolve bind interface", e);
			return NetworkConstants.LOOPBACK_IF;
		}
	}

	/** multicast reader group address */
	protected NetAddress getGroupAddress() {
		return getNetPoint().getRemoteAddress();
	}

	protected NetAddress getLocalAddress() {
		return getNetPoint().getLocalAddress();
	}

	/** valid bind address or local host */
	/** FIXME slow dns lookup */
	protected NetAddress getBindAddress() {
		try {
			/**
			 * allows to avoid duplicate packets when multiple multicast groups
			 * share the same port
			 */
			final InetAddress bindAddr;
			final int bindPort = getGroupAddress().getPort();
			switch (OperatingSystem.CURRENT) {
			case LINUX:
				bindAddr = getGroupAddress().getAddress();
				break;
			case WINDOWS:
				bindAddr = getLocalAddress().getAddress();
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
	protected void activateBoot() throws Exception {

		boot().localAddress(getLocalAddress()); // XXX bind address

		boot().remoteAddress(getGroupAddress());

		boot().option(ChannelOption.SO_REUSEADDR, true);

		boot().option(ChannelOption.IP_MULTICAST_TTL,
				getNetPoint().getPacketTTL());

		boot().group(group());

		boot().channel(channel());

		boot().handler(handler());

		boot().bind().sync();

		channel().joinGroup(getGroupAddress(), getBindInteface()).sync();

	}

	@Override
	protected void deactivateBoot() throws Exception {

		channel().leaveGroup(getGroupAddress(), getBindInteface()).sync();

		channel().close().sync();

	}

}
