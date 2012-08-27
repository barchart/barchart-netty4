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
