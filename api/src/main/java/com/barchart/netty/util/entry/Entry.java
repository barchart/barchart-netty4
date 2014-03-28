/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.util.entry;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

/**
 * {@link Config} Entry.
 * <p>
 * TODO move to conf-util
 */
public abstract class Entry {

	private final Config config;

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected Entry(final Config config) {
		this.config = config;
	}

	public Config config() {
		return config;
	}

	public double getDouble(final String path, final double value) {
		try {
			return config().getDouble(path);
		} catch (final Exception e) {
			log.error("", e);
			return value;
		}
	}

	public int getInt(final String path, final int value) {
		try {
			return config().getInt(path);
		} catch (final Exception e) {
			log.error("", e);
			return value;
		}
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getUniformList(final String path, final Class<T> klaz) {
		try {

			if (klaz == String.class) {
				return (List<T>) config().getStringList(path);
			}

			if (klaz == Integer.class) {
				return (List<T>) config().getIntList(path);
			}

			if (klaz == Long.class) {
				return (List<T>) config().getLongList(path);
			}

			if (klaz == Double.class) {
				return (List<T>) config().getDoubleList(path);
			}

			if (klaz == Boolean.class) {
				return (List<T>) config().getBooleanList(path);
			}

			throw new IllegalArgumentException("expecting primitive types");

		} catch (final Exception e) {
			log.error("", e);
			return Collections.emptyList();
		}

	}

	public long getLong(final String path, final long value) {
		try {
			return config().getInt(path);
		} catch (final Exception e) {
			log.error("", e);
			return value;
		}
	}

	public String getString(final String path, final String value) {
		try {
			return config().getString(path);
		} catch (final Exception e) {
			log.error("", e);
			return value;
		}
	}

	@Override
	public String toString() {
		return config().root().render();
	}

}
