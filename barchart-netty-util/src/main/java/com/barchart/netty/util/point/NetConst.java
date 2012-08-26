package com.barchart.netty.util.point;

public interface NetConst {

	/**
	 * host and port
	 * 
	 * "datalan:12345"
	 * 
	 * "datalan/12345"
	 * 
	 * "datalan 12345"
	 * 
	 */
	String ADDRESS_REGEX = "([^:/\\s]*)([:/\\s]*)([^:/\\s]*)";

	int DEFAULT_BUFFER_SIZE = 100 * 1024;

}
