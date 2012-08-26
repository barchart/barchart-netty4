package com.barchart.netty.util.point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.barchart.netty.util.entry.Entry;
import com.typesafe.config.Config;

/** network end point */
public class NetPoint extends Entry implements Comparable<NetPoint>, NetKey {

	public NetAddress getNetAddress(final String key) {
		final Object address = load(key);
		if (address instanceof String) {
			return NetAddress.formTuple((String) address);
		}
		return null;
	}

	public void setNetAddress(final String key, final NetAddress address) {
		if (key == null || address == null) {
			return;
		}
		save(key, address.toString());
	}

	public NetAddress getLocalAddress() {
		return getNetAddress(KEY_LOCAL_ADDRESS);
	}

	public int getPacketTTL() {
		return getInt(KEY_PACKET_TTL);
	}

	//

	public int getReceiveBufferSize() {
		return getInt(KEY_RECV_BUF_SIZE);
	}

	public NetAddress getRemoteAddress() {
		return getNetAddress(KEY_REMOTE_ADDRESS);
	}

	public String getPipeline() {
		return load(KEY_PIPELINE);
	}

	public int getSendBufferSize() {
		return getInt(KEY_SEND_BUF_SIZE);
	}

	public boolean isValidLocal() {
		return getLocalAddress() != null;
	}

	public boolean isValidRemote() {
		return getRemoteAddress() != null;
	}

	public void setLocalAddress(final NetAddress localAddress) {
		setNetAddress(KEY_LOCAL_ADDRESS, localAddress);
	}

	public void setPacketTTL(final int packetTTL) {
		save(KEY_PACKET_TTL, packetTTL);
	}

	public void setReceiveBufferSize(final int receiveBufferSize) {
		save(KEY_RECV_BUF_SIZE, receiveBufferSize);
	}

	public void setRemoteAddress(final NetAddress remoteAddress) {
		setNetAddress(KEY_REMOTE_ADDRESS, remoteAddress);
	}

	public void setPipeline(final String pipeline) {
		save(KEY_PIPELINE, pipeline);
	}

	public void setSendBufferSize(final int sendBufferSize) {
		save(KEY_SEND_BUF_SIZE, sendBufferSize);
	}

	public String identity() {
		return getPipeline() + ";" + getLocalAddress() + ";"
				+ getRemoteAddress();
	}

	@Override
	public boolean equals(final Object other) {
		if (other instanceof NetPoint) {
			final NetPoint that = (NetPoint) other;
			return that.identity().equals(this.identity());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return identity().hashCode();
	}

	public boolean isValidMulticastReader() {
		return isValidLocal() && isValidRemote()
				&& NetUtil.isValidMulticastAddress(getRemoteAddress());
	}

	public boolean isValidMulticastWriter() {
		return isValidLocal() && isValidRemote()
				&& NetUtil.isValidMulticastAddress(getRemoteAddress())
				&& 0 <= getPacketTTL() && getPacketTTL() <= 0xFF;
	}

	public boolean isValidDatagramReader() {
		return isValidLocal() && isValidRemote();
	}

	public boolean isValidDatagramWriter() {
		return isValidLocal() && isValidRemote();
	}

	public boolean isValidStreamClient() {
		return isValidLocal() && isValidRemote();
	}

	public boolean isValidStreamServer() {
		return isValidLocal() && !isValidRemote();
	}

	@Override
	public int compareTo(final NetPoint that) {
		if (that == null) {
			return 0;
		}
		return this.identity().compareTo(that.identity());
	}

	public static NetPoint from(final Map<String, Object> map) {
		final NetPoint entry = new NetPoint();
		entry.props().putAll(map);
		return entry;
	}

	public static NetPoint from(final Config conf) {
		return from(conf.root().unwrapped());
	}

	public static List<NetPoint> form(final List<? extends Config> confList) {

		final List<NetPoint> pointList = new ArrayList<NetPoint>(
				confList.size());

		for (final Config conf : confList) {
			final NetPoint point = from(conf);
			pointList.add(point);
		}

		return pointList;

	}

}
