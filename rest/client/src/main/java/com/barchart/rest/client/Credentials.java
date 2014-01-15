package com.barchart.rest.client;

public interface Credentials {

	void authenticate(RestRequest<?> request);

}
