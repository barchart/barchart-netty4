package com.barchart.netty.util.point;

public interface NetConst {

	/**
	 * supported patterns:
	 * 
	 * host and port:
	 * 
	 * "datalan:12345" ;
	 * 
	 * "datalan/12345" ;
	 * 
	 * "datalan 12345" ;
	 * 
	 */
	String ADDRESS_REGEX = "([^:/\\s]*)([:/\\s]*)([^:/\\s]*)";

	int DEFAULT_BUFFER_SIZE = 100 * 1024;

	int DEFAULT_PACKET_TTL = 3;

}
