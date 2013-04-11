package bench.addr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.util.point.NetAddress;

public class MainAddr {

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	final static Logger log = LoggerFactory.getLogger(MainAddr.class);

	public static void main(final String... args) throws Exception {

		log.info("init");

		{

			final String groupAddr = "239.1.2.3";
			final int groupPort = 55555;

			final NetAddress addr1 = new NetAddress(groupAddr, groupPort);

			final NetAddress addr2 = NetAddress.formTuple("239.1.2.3/55555");

		}

		{

			final String groupAddr = "localhost";
			final int groupPort = 0;

			final NetAddress addr1 = new NetAddress(groupAddr, groupPort);

			final NetAddress addr2 = NetAddress.formTuple("localhost/0");

		}

		log.info("done");

	}

}
