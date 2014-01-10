package com.barchart.netty.common.util;

import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Network utilities.
 */
public final class AddressUtil {

	public static Pattern ADDRESS_REGEX = Pattern
			.compile("([^:/\\s]*)([:/\\s]*)([^:/\\s]*)");

	public static boolean isValidMulticastAddress(
			final InetSocketAddress address) {
		return address != null && address.getAddress() != null
				&& address.getAddress().isMulticastAddress();
	}

	public static boolean isValidAddress(final String address) {
		return ADDRESS_REGEX.matcher(address).matches();
	}

	/**
	 * Create an InetSocketAddress from a String input of the form "X.X.X.X:Y"
	 */
	public static InetSocketAddress parseAddress(final String address) {

		final String host;
		final int port;

		if (address == null) {
			host = "0.0.0.0";
			port = 0;
		} else {
			final Matcher matcher = ADDRESS_REGEX.matcher(address);
			if (matcher.matches()) {
				host = matcher.group(1);
				port = AddressUtil.safePort(matcher.group(3));
			} else {
				host = "0.0.0.0";
				port = 0;
			}
		}

		return new InetSocketAddress(host, port);

	}

	/**
	 * Validate the given port is in a valid range, returning 0 if it is
	 * invalid.
	 */
	public static int safePort(final String port) {
		try {
			final int number = Integer.parseInt(port);
			if (number < 0 || number > 65535) {
				return 0;
			} else {
				return number;
			}
		} catch (final Throwable e) {
			return 0;
		}
	}

}
