package com.barchart.netty.util.point;



public final class NetUtil {

	public static boolean isValidMulticastAddress(final NetAddress netAddress) {
		return netAddress != null && netAddress.getAddress() != null
				&& netAddress.getAddress().isMulticastAddress();
	}

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
