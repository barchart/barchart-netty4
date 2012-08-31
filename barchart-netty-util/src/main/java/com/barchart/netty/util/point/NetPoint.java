package com.barchart.netty.util.point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.barchart.netty.util.entry.Entry;
import com.typesafe.config.Config;

/** network end point */
public class NetPoint extends Entry implements Comparable<NetPoint>, NetKey,
		NetConst {

	public static List<NetPoint> form(final List<? extends Config> confList) {

		final List<NetPoint> pointList = new ArrayList<NetPoint>(
				confList.size());

		for (final Config conf : confList) {
			final NetPoint point = from(conf);
			pointList.add(point);
		}

		return pointList;

	}

	public static NetPoint from(final Config conf) {
		return from(conf.root().unwrapped());
	}

	public static NetPoint from(final Map<String, Object> map) {
		final NetPoint entry = new NetPoint();
		entry.props().putAll(map);
		return entry;
	}

	@Override
	public int compareTo(final NetPoint that) {
		if (that == null) {
			return 0;
		}
		return this.identity().compareTo(that.identity());
	}

	//

	@Override
	public boolean equals(final Object other) {
		if (other instanceof NetPoint) {
			final NetPoint that = (NetPoint) other;
			return that.identity().equals(this.identity());
		}
		return false;
	}

	public NetAddress getLocalAddress() {
		return getNetAddress(KEY_LOCAL_ADDRESS);
	}

	public String getManagedPipeline() {
		return load(KEY_MANAGED_PIPELINE);
	}

	public NetAddress getNetAddress(final String key) {
		final Object address = load(key);
		if (address instanceof String) {
			return NetAddress.formTuple((String) address);
		}
		return null;
	}

	public int getPacketTTL() {
		return getInt(KEY_PACKET_TTL, DEFAULT_PACKET_TTL);
	}

	public String getPipeline() {
		return load(KEY_PIPELINE);
	}

	public int getReceiveBufferSize() {
		return getInt(KEY_RECV_BUF_SIZE, DEFAULT_BUFFER_SIZE);
	}

	public NetAddress getRemoteAddress() {
		return getNetAddress(KEY_REMOTE_ADDRESS);
	}

	public int getSendBufferSize() {
		return getInt(KEY_SEND_BUF_SIZE, DEFAULT_BUFFER_SIZE);
	}

	public String getType() {
		return load(KEY_TYPE);
	}

	@Override
	public int hashCode() {
		return identity().hashCode();
	}

	public String identity() {
		return getManagedPipeline() + ";" + getLocalAddress() + ";"
				+ getRemoteAddress();
	}

	public boolean isValidDatagramReader() {
		return isValidLocal() && isValidRemote();
	}

	public boolean isValidDatagramWriter() {
		return isValidLocal() && isValidRemote();
	}

	public boolean isValidLocal() {
		return getLocalAddress() != null;
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

	public boolean isValidRemote() {
		return getRemoteAddress() != null;
	}

	public boolean isValidStreamClient() {
		return isValidLocal() && isValidRemote();
	}

	public boolean isValidStreamServer() {
		return isValidLocal() && !isValidRemote();
	}

	public void setLocalAddress(final NetAddress localAddress) {
		setNetAddress(KEY_LOCAL_ADDRESS, localAddress);
	}

	public void setManagedPipeline(final String pipeline) {
		save(KEY_MANAGED_PIPELINE, pipeline);
	}

	public void setNetAddress(final String key, final NetAddress address) {
		if (key == null || address == null) {
			return;
		}
		save(key, address.toString());
	}

	public void setPacketTTL(final int packetTTL) {
		save(KEY_PACKET_TTL, packetTTL);
	}

	public void setPipeline(final String pipeline) {
		save(KEY_PIPELINE, pipeline);
	}

	public void setReceiveBufferSize(final int receiveBufferSize) {
		save(KEY_RECV_BUF_SIZE, receiveBufferSize);
	}

	public void setRemoteAddress(final NetAddress remoteAddress) {
		setNetAddress(KEY_REMOTE_ADDRESS, remoteAddress);
	}

	public void setSendBufferSize(final int sendBufferSize) {
		save(KEY_SEND_BUF_SIZE, sendBufferSize);
	}

	public void setType(final String type) {
		save(KEY_TYPE, type);
	}

}
