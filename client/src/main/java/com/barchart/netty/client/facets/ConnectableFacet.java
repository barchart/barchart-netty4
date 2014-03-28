/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.client.facets;

import com.barchart.netty.common.pipeline.PipelineInitializer;

/**
 * A facet for dynamically constructing client proxies with pluggable
 * functionality.
 * 
 * @See com.barchart.netty.client.base.ConnectableProxy
 */
public interface ConnectableFacet<U> extends PipelineInitializer {

	/**
	 * The interface that the constructed ConnectableProxy should implement for
	 * this facet.
	 */
	Class<? super U> type();

}
