package bench.nio;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainIface {

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	final static Logger log = LoggerFactory.getLogger(MainIface.class);

	public static void main(final String... args) throws Exception {

		log.info("init");

		{

			final InetAddress ifaceAddr = InetAddress.getByName("localhost");

			final NetworkInterface iface = NetworkInterface
					.getByInetAddress(ifaceAddr);

			log.info("iface = " + iface);

		}

		{

			final Enumeration<NetworkInterface> list = NetworkInterface
					.getNetworkInterfaces();

			while (list.hasMoreElements()) {

				final NetworkInterface iface = list.nextElement();

				log.info("iface = " + iface);
				log.info("iface index = " + iface.getIndex());
				log.info("iface loop  = " + iface.isLoopback());
				log.info("iface up    = " + iface.isUp());

			}

		}

		log.info("done");

	}

}