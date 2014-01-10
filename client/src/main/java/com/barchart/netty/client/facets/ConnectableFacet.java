package com.barchart.netty.client.facets;

import com.barchart.netty.common.PipelineInitializer;

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
