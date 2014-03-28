/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.matrix.api;

public interface Matrix {

	String PROP_MATRIX_TARGET_FILTER = "matrix-target-filter";

	//

	void configCleanup(final String targetId);

	void configApply(final MatrixConfig config);

	void process(final String sourceId, final Object message);

	void setTargetFilter(String targetFilter);

}