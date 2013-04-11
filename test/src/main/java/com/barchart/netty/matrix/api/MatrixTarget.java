package com.barchart.netty.matrix.api;

public interface MatrixTarget {

	String getId();

	String getFilter();

	void process(Object message);

}
