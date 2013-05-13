package com.barchart.netty.util.point;

/**
 * Network constants.
 */
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

}
