/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.util.point;

/**
 * Network utilities.
 */
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
