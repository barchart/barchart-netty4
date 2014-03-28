/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
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
