package com.barchart.netty.matrix.api;

public interface Matrix {

	String PROP_MATRIX_TARGET_FILTER = "matrix-target-filter";

	//

	void configCleanup(final String targetId);

	void configApply(final MatrixConfig config);

	void process(final String sourceId, final Object message);

	void setTargetFilter(String targetFilter);

}