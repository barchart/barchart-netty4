package com.barchart.netty.part.dot;

import io.netty.util.NetworkConstants;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.host.impl.OperatingSystem;
import com.barchart.netty.util.point.NetAddress;
import com.barchart.osgi.factory.api.FactoryDescriptor;

/**
 * parent for multicast end points
 * 
 * handles multicast join / leave;
 */
@Component(factory = DotCastMulti.FACTORY)
public class DotCastMulti extends DotCast {

	public static final String FACTORY = "barchart.netty.dot.cast.multi";

	@Override
	public String getFactoryId() {
		return FACTORY;
	}

	@FactoryDescriptor
	private static final Map<String, String> descriptor;
	static {
		descriptor = new HashMap<String, String>();
		descriptor.put(PROP_FACTORY_ID, FACTORY);
		descriptor.put(PROP_FACTORY_DESCRIPTION,
				"multicast reader/writer end point service");
	}

	/** valid interface or local host */
	protected NetworkInterface getBindInteface() {
		try {
			return NetworkInterface.getByInetAddress(//
					localAddress().getAddress());
		} catch (final Throwable e) {
			log.error("fatal: can not resolve bind interface", e);
			return NetworkConstants.LOOPBACK_IF;
		}
	}

	/** multicast reader group address */
	protected NetAddress getGroupAddress() {
		return getNetPoint().getRemoteAddress();
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
				bindAddr = localAddress().getAddress();
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

		super.activateBoot();

		channel().joinGroup(getGroupAddress(), getBindInteface()).sync();

	}

	@Override
	protected void deactivateBoot() throws Exception {

		channel().leaveGroup(getGroupAddress(), getBindInteface()).sync();

		super.deactivateBoot();

	}

}
