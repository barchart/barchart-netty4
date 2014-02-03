package com.barchart.netty.rest.client;

/**
 * Marker interface for REST clients.
 */
public interface RestClient {

	Credentials credentials();

	/**
	 * Set the authentication credentials for future requests.
	 */
	void credentials(final Credentials credentials);

}
