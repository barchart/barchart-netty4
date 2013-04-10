package bench.nio_mc;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProtocolFamily;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Reader {

	static void usage() {

		System.err
				.println("usage: java Reader group:port@interf [-only source...] [-block source...]");

		System.exit(-1);

	}

	static void printDatagram(final SocketAddress sa, final ByteBuffer buf) {

		System.out.format("-- datagram from %s --\n", ((InetSocketAddress) sa)
				.getAddress().getHostAddress());

		System.out.println(Charset.defaultCharset().decode(buf));

	}

	static void parseAddessList(final String s, final List<InetAddress> list)
			throws UnknownHostException {

		final String[] sources = s.split(",");

		for (int i = 0; i < sources.length; i++) {
			list.add(InetAddress.getByName(sources[i]));
		}

	}

	public static void main(final String[] args) throws IOException {

		if (args.length == 0)
			usage();

		// first parameter is the multicast address (interface required)
		final MulticastAddress target = MulticastAddress.parse(args[0]);

		if (target.interf() == null)
			usage();

		// addition arguments are source addresses to include or exclude
		final List<InetAddress> includeList = new ArrayList<InetAddress>();
		final List<InetAddress> excludeList = new ArrayList<InetAddress>();

		int argc = 1;

		while (argc < args.length) {

			final String option = args[argc++];

			if (argc >= args.length)
				usage();

			final String value = args[argc++];

			if (option.equals("-only")) {
				parseAddessList(value, includeList);
				continue;
			}

			if (option.equals("-block")) {
				parseAddessList(value, excludeList);
				continue;
			}

			usage();

		}

		if (!includeList.isEmpty() && !excludeList.isEmpty()) {
			usage();
		}

		// create and bind socket

		ProtocolFamily family = StandardProtocolFamily.INET;
		if (target.group() instanceof Inet6Address) {
			family = StandardProtocolFamily.INET6;
		}

		final DatagramChannel dc = DatagramChannel.open(family)
				.setOption(StandardSocketOptions.SO_REUSEADDR, true)
				.bind(new InetSocketAddress(target.port()));

		if (includeList.isEmpty()) {

			// join group and block addresses on the exclude list
			final MembershipKey key = dc.join(target.group(), target.interf());

			for (final InetAddress source : excludeList) {
				key.block(source);
			}

		} else {

			// join with source-specific membership for each source
			for (final InetAddress source : includeList) {
				dc.join(target.group(), target.interf(), source);
			}

		}

		// register socket with Selector
		final Selector sel = Selector.open();

		dc.configureBlocking(false);

		dc.register(sel, SelectionKey.OP_READ);

		// print out each datagram that we receive
		final ByteBuffer buf = ByteBuffer.allocateDirect(4096);

		for (;;) {
			final int updated = sel.select();
			if (updated > 0) {
				final Iterator<SelectionKey> iter = sel.selectedKeys()
						.iterator();
				while (iter.hasNext()) {
					final SelectionKey sk = iter.next();
					iter.remove();

					final DatagramChannel ch = (DatagramChannel) sk.channel();
					final SocketAddress sa = ch.receive(buf);
					if (sa != null) {
						buf.flip();
						printDatagram(sa, buf);
						buf.rewind();
						buf.limit(buf.capacity());
					}
				}

			}

		}

	}

}
