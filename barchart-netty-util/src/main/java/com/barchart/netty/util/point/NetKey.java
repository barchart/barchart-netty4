package com.barchart.netty.util.point;

public interface NetKey {

	String KEY_LOCAL_ADDRESS = "local-address";

	String KEY_REMOTE_ADDRESS = "remote-address";

	String KEY_PACKET_TTL = "packet-ttl";

	String KEY_RECV_BUF_SIZE = "receive-buffer-size";

	String KEY_SEND_BUF_SIZE = "send-buffer-size";

	/** end point type (dot factory) */
	String KEY_TYPE = "type";

	/** default(parent) channel pipeline */
	String KEY_PIPELINE = "pipeline";

	/** managed(child) channel pipeline name */
	String KEY_MANAGED_PIPELINE = "managed-pipeline";

}
