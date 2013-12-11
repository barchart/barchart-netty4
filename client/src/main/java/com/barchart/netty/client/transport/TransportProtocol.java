package com.barchart.netty.client.transport;

import io.netty.channel.Channel;
import io.netty.channel.sctp.nio.NioSctpChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.udt.nio.NioUdtByteConnectorChannel;

import com.barchart.netty.client.PipelineInitializer;

public interface TransportProtocol extends PipelineInitializer {

	public enum Event {
		CONNECTED, DISCONNECTED
	}

	/**
	 * TCP transport.
	 */
	public static final TransportProtocol TCP = new SimpleTransport(
			NioSocketChannel.class);

	/**
	 * UDP transport
	 */
	public static final TransportProtocol UDP = new SimpleTransport(
			NioDatagramChannel.class);

	/**
	 * UDP-based reliable data transfer transport
	 */
	public static final TransportProtocol UDT = new SimpleTransport(
			NioUdtByteConnectorChannel.class);

	/**
	 * SCTP transport
	 */
	public static final TransportProtocol SCTP = new SimpleTransport(
			NioSctpChannel.class);

	public Class<? extends Channel> channel();

}