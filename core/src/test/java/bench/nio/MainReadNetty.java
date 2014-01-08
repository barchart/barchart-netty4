package bench.nio;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainReadNetty {

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	final static Logger log = LoggerFactory.getLogger(MainReadNetty.class);

	public static void main(final String... args) throws Exception {

		log.info("init");

		//

		final String host = "mainlan";

		final InetAddress ifaceAddr = InetAddress.getByName(host);

		final NetworkInterface iface =
				NetworkInterface.getByInetAddress(ifaceAddr);
		log.info("iface = " + iface);

		//

		final String groupAddr = "239.1.2.3";
		final int groupPort = 12345;

		//

		/** NOTE: */
		final SocketAddress local = new InetSocketAddress("0.0.0.0", groupPort);
		log.info("local  = " + local);

		final InetSocketAddress remote =
				new InetSocketAddress(groupAddr, groupPort);
		log.info("remote  = " + remote);

		//

		final Bootstrap boot = new Bootstrap();
		final NioDatagramChannel channel = new NioDatagramChannel();

		boot.localAddress(local);
		boot.remoteAddress(remote);

		boot.option(ChannelOption.SO_REUSEADDR, true);

		boot.option(ChannelOption.IP_MULTICAST_TTL, 123);

		boot.group(new NioEventLoopGroup());
		boot.channel(NioDatagramChannel.class);
		boot.handler(new LoggingHandler());

		boot.bind().sync();

		channel.joinGroup(remote, iface).sync();

		log.info("done");

	}

	static void printDatagram(final SocketAddress sa, final ByteBuffer buf) {

		System.out.format("-- datagram from %s --\n", ((InetSocketAddress) sa)
				.getAddress().getHostAddress());

		System.out.println(Charset.defaultCharset().decode(buf));

	}

}
