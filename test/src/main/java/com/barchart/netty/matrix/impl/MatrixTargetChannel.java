/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.matrix.impl;

import io.netty.channel.Channel;

import com.barchart.netty.matrix.api.MatrixTarget;

public class MatrixTargetChannel implements MatrixTarget {

	private final Channel channel;

	public MatrixTargetChannel(final Channel channel) {
		this.channel = channel;
	}

	@Override
	public String getId() {
		return "";
	}

	@Override
	public void process(final Object message) {
		channel.write(message);
	}

	@Override
	public String getFilter() {
		return null;
	}

}
