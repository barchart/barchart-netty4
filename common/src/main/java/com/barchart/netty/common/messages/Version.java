/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.common.messages;

public class Version implements Comparable<Version> {

	private final String version;
	private final String[] parts;

	public Version(final String version_) {
		version = version_.trim();
		parts = version.split("\\.");
	}

	@Override
	public int compareTo(final Version that) {

		int i = 0;

		while (i < parts.length && i < that.parts.length
				&& parts[i].equals(that.parts[i])) {
			i++;
		}

		if (i < parts.length && i < that.parts.length) {
			final int diff =
					Integer.valueOf(parts[i]).compareTo(
							Integer.valueOf(that.parts[i]));
			return Integer.signum(diff);
		}

		return Integer.signum(parts.length - that.parts.length);

	}

	@Override
	public boolean equals(final Object o) {

		if (o instanceof Version) {
			return compareTo((Version) o) == 0;
		}

		return false;

	}

	public boolean lessThan(final Version that) {
		return compareTo(that) < 0;
	}

	public boolean lessThanOrEqual(final Version that) {
		return compareTo(that) <= 0;
	}

	public boolean greaterThan(final Version that) {
		return compareTo(that) > 0;
	}

	public boolean greaterThanOrEqual(final Version that) {
		return compareTo(that) >= 0;
	}

	public boolean inRange(final Version lower, final Version upper) {
		return greaterThanOrEqual(lower) && lessThanOrEqual(upper);
	}

	@Override
	public String toString() {
		return version;
	}

}
