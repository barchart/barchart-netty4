package bench.nio;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainWriteNetty {

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	final static Logger log = LoggerFactory.getLogger(MainWriteNetty.class);

	public static void main(final String... args) throws Exception {

		log.info("init");

		//

		final String host = "mainlan";

		final InetAddress ifaceAddr = InetAddress.getByName(host);
		final NetworkInterface iface =
				NetworkInterface.getByInetAddress(ifaceAddr);

		log.info("iface = " + iface); //

		//

		final String groupAddr = "239.1.2.3";
		final int groupPort = 12345;

		//

		final InetAddress group = InetAddress.getByName(groupAddr);
		log.info("group = " + group);

		final SocketAddress local = new InetSocketAddress(host, 0);
		log.info("bind  = " + local);

		final InetSocketAddress remote =
				new InetSocketAddress(groupAddr, groupPort);

		final Bootstrap boot = new Bootstrap();
		boot.localAddress(local);
		boot.remoteAddress(remote);
		boot.option(ChannelOption.SO_REUSEADDR, true);
		boot.option(ChannelOption.IP_MULTICAST_TTL, 123);
		boot.group(new NioEventLoopGroup());
		boot.channel(NioDatagramChannel.class);
		boot.handler(new LoggingHandler());
		final ChannelFuture future = boot.bind().sync();

		final Channel channel = future.channel();

		for (int k = 0; k < 100; k++) {

			final byte[] array = new String("hello kitty " + k).getBytes();

			final ByteBuf buffer = Unpooled.wrappedBuffer(array);

			final DatagramPacket packet = new DatagramPacket(buffer, remote);

			channel.write(packet);

			log.debug("write : {}", k);

			Thread.sleep(1 * 1000);

		}

		log.info("done");

	}

}
