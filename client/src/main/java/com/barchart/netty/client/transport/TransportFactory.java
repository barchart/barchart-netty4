package com.barchart.netty.client.transport;

import java.net.URI;

public class TransportFactory {

	public static TransportProtocol create(final String url) {
		return create(URI.create(url));
	}

	/**
	 * Get the transport protocol from a URI. URIs are of the form:
	 * 
	 * <pre>proto://host:port[/path]</pre>
	 * 
	 * Supported protocols are: <code>tcp</code>, <code>udp</code>,
	 * <code>udt</code>, <code>sctp</code>, <code>ws</code>, and
	 * <code>wss</code>.
	 * 
	 * The <code>/path</code> portion only applies to websocket connections (
	 * <code>ws/wss</code>).
	 * 
	 * @param uri
	 * @return
	 */
	public static TransportProtocol create(final URI uri) {

		final String scheme = uri.getScheme();

		if ("tcp".equals(scheme)) {
			return new TCPTransport(uri);
		} else if ("udp".equals(scheme)) {
			return new UDPTransport(uri);
		} else if ("udt".equals(scheme)) {
			return new UDTTransport(uri);
		} else if ("sctp".equals(scheme)) {
			return new SCTPTransport(uri);
		} else if ("ws".equals(scheme)) {
			return new WebSocketTransport(uri);
		} else if ("wss".equals(scheme)) {
			return new WebSocketTransport(uri);
		}

		throw new IllegalArgumentException("Unsupported transport scheme: "
				+ scheme);

	}

}
