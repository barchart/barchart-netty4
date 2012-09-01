package com.barchart.netty.util.point;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class NetAddress extends InetSocketAddress {

	protected static final Logger log = LoggerFactory
			.getLogger(NetAddress.class);

	static final String PROP_IP4 = "java.net.preferIPv4Stack";

	static {
		System.setProperty(PROP_IP4, "true");
		log.info("NOTE: {}={}", PROP_IP4, System.getProperty(PROP_IP4));
	}

	private final String host;

	/**
	 * original host name unaffected by DNS resolution; this is different from
	 * {@link #getHostName()} which calls DNS resolve;
	 */
	public String getHost() {
		return host;
	}

	public NetAddress(final String host, final int port) {
		super(host, port);
		this.host = host;
	}

	public NetAddress(final InetAddress addr, final int port) {
		super(addr, port);
		this.host = addr.getHostName();
	}

	static final Pattern pattern = Pattern.compile(NetConst.ADDRESS_REGEX);

	/** from tuple : "host:port" */
	// @JsonCreator
	public static NetAddress formTuple(final String address) {

		final String host;
		final int port;

		if (address == null) {
			host = "0.0.0.0";
			port = 0;
		} else {
			final Matcher matcher = pattern.matcher(address);
			if (matcher.matches()) {
				host = matcher.group(1);
				port = NetUtil.safePort(matcher.group(3));
			} else {
				host = "0.0.0.0";
				port = 0;
			}
		}

		return new NetAddress(host, port);

	}

	@Override
	public String toString() {
		return intoTuple();
	}

	/**
	 * use original(non resolved) host name and ":" separator
	 * 
	 * "host:port"
	 */
	// @JsonValue
	public String intoTuple() {
		return getHost() + ":" + getPort();
	}

}
