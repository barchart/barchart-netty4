package com.barchart.netty.util.point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class NetPointXXX implements NetKey {

	public static final Config reference() {
		return ConfigFactory.defaultReference(
				NetPointXXX.class.getClassLoader()).getConfig("net-point");
	}

	private final Config config;

	public Config config() {
		return config;
	}

	private NetPointXXX(final Config config) {
		this.config = config;
	}

	public static NetPointXXX from(final Config config) {
		if (config == null) {
			return new NetPointXXX(reference());
		} else {
			return new NetPointXXX(config.withFallback(reference()));
		}
	}

	public String id() {
		return config.getString(KEY_ID);
	}

	public String type() {
		return config.getString(KEY_TYPE);
	}

	public String pipeline() {
		return config.getString(KEY_PIPELINE);
	}

	public NetAddress localAddress() {
		return NetAddress.formTuple(config.getString(KEY_LOCAL_ADDRESS));
	}

	public NetAddress remoteAddress() {
		return NetAddress.formTuple(config.getString(KEY_REMOTE_ADDRESS));
	}

	public int receiveBufferSize() {
		return config.getInt(KEY_RECV_BUF_SIZE);
	}

	public int sendBufferSize() {
		return config.getInt(KEY_SEND_BUF_SIZE);
	}

	public int packetTTL() {
		return config.getInt(KEY_PACKET_TTL);
	}

	//

	public String getString(final String key, final String value) {
		try {
			return config.getString(key);
		} catch (final Exception e) {
			return value;
		}
	}

	public int getInt(final String key, final int value) {
		try {
			return config.getInt(key);
		} catch (final Exception e) {
			return value;
		}
	}

	public long getLong(final String key, final long value) {
		try {
			return config.getInt(key);
		} catch (final Exception e) {
			return value;
		}
	}

	public double getDouble(final String key, final double value) {
		try {
			return config.getDouble(key);
		} catch (final Exception e) {
			return value;
		}
	}

	public <T> List<T> getList(final String key) {
		if (key == null) {
			return Collections.emptyList();
		} else {
			return null;
		}
	}

	//

	public static List<NetPointXXX> form(final List<? extends Config> confList) {

		final List<NetPointXXX> pointList = new ArrayList<NetPointXXX>(
				confList.size());

		for (final Config config : confList) {
			final NetPointXXX point = from(config);
			pointList.add(point);
		}

		return pointList;

	}

}
