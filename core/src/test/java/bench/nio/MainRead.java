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
import java.nio.channels.MembershipKey;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainRead {

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	final static Logger log = LoggerFactory.getLogger(MainRead.class);

	public static void main(final String... args) throws Exception {

		log.info("init");

		//

		final InetAddress ifaceAddr = InetAddress.getByName("localhost");
		final NetworkInterface iface = NetworkInterface
				.getByInetAddress(ifaceAddr);

		log.info("iface = " + iface); //

		//

		final String groupAddr = "239.1.2.3";
		final int groupPort = 12345;

		//

		final InetAddress group = InetAddress.getByName(groupAddr);
		log.info("group = " + group);

		final SocketAddress bind = new InetSocketAddress(groupPort);
		log.info("bind  = " + bind);

		final DatagramChannel channel = DatagramChannel
				.open(StandardProtocolFamily.INET)
				.setOption(StandardSocketOptions.SO_REUSEADDR, true).bind(bind)
				.setOption(StandardSocketOptions.IP_MULTICAST_IF, iface);

		final MembershipKey key = channel.join(group, iface);

		//

		channel.configureBlocking(false);

		final Selector selector = Selector.open();

		channel.register(selector, SelectionKey.OP_READ);

		final ByteBuffer buffer = ByteBuffer.allocateDirect(4096);

		for (int k = 0; k < 100; k++) {

			log.info("select");

			final int updated = selector.select();

			if (updated > 0) {

				final Iterator<SelectionKey> iter = selector.selectedKeys()
						.iterator();

				while (iter.hasNext()) {

					final SelectionKey sk = iter.next();

					iter.remove();

					final DatagramChannel ch = (DatagramChannel) sk.channel();

					final SocketAddress source = ch.receive(buffer);

					if (source != null) {
						buffer.flip();
						printDatagram(source, buffer);
						buffer.rewind();
						buffer.limit(buffer.capacity());
					}

				}

			}

		}

		log.info("done");

	}

	static void printDatagram(final SocketAddress sa, final ByteBuffer buf) {

		System.out.format("-- datagram from %s --\n", ((InetSocketAddress) sa)
				.getAddress().getHostAddress());

		System.out.println(Charset.defaultCharset().decode(buf));

	}

}
