/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.http.request;

/**
 * An arbitrarily-valued attribute for storing in a ServerRequest object.
 */
public class RequestAttribute<T> {

	private T value = null;

	public void set(final T value_) {
		value = value_;
	}

	public T get() {
		return value;
	}

}
