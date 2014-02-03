package com.barchart.netty.rest.client;

import java.util.List;
import java.util.Map;

/**
 * Represents a REST response.
 * 
 * @param <T> The data type decoded from the body.
 */
public interface RestResponse<T> {

	/**
	 * Whether this response was successful or not.
	 */
	boolean success();

	/**
	 * HTTP status code for this response.
	 */
	int status();

	/**
	 * The error message if the request failed.
	 */
	String error();

	/**
	 * Response headers from the remote server.
	 */
	Map<String, List<String>> headers();

	/**
	 * Decoded content from the response body.
	 */
	T content();

}