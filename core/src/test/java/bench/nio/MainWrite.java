/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package bench.nio;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainWrite {

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	final static Logger log = LoggerFactory.getLogger(MainWrite.class);

	public static void main(final String... args) throws Exception {

		log.info("init");

		//

		final String host = "mainlan";

		final InetAddress ifaceAddr = InetAddress.getByName(host);
		final NetworkInterface iface = NetworkInterface
				.getByInetAddress(ifaceAddr);

		log.info("iface = " + iface); //

		//

		final String groupAddr = "239.1.2.3";
		final int groupPort = 12345;

		//

		final InetAddress group = InetAddress.getByName(groupAddr);
		log.info("group = " + group);

		final SocketAddress bind = new InetSocketAddress(host, 0);
		log.info("bind  = " + bind);

		final DatagramChannel channel = DatagramChannel
				.open(StandardProtocolFamily.INET)
				.setOption(StandardSocketOptions.IP_MULTICAST_TTL, 123)
				.setOption(StandardSocketOptions.IP_MULTICAST_IF, iface)
				.setOption(StandardSocketOptions.SO_REUSEADDR, true).bind(bind)
		//
		;

		//

		final SocketAddress remote = new InetSocketAddress(groupAddr, groupPort);

		channel.connect(remote);

		channel.configureBlocking(true);

		for (int k = 0; k < 10 * 1000; k++) {

			final byte[] array = new String("hello kitty " + k).getBytes();

			final ByteBuffer buffer = ByteBuffer.wrap(array);

			channel.write(buffer);

			// log.debug("write : {}", k);

			Thread.sleep(1 * 1000);

		}

		log.info("done");

	}

}
