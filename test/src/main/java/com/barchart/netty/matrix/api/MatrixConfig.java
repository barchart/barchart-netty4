/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.matrix.api;

public interface MatrixConfig {

	String KEY_ACTIVE = "active";
	String KEY_SOURCE = "source";
	String KEY_TARGET = "target";

	//

	boolean isValid();

	boolean isActive();

	CharSequence getSourceId();

	CharSequence getTargetId();

}
