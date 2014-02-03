package com.barchart.netty.rest.client;

public interface Credentials {

	void authenticate(RestRequest<?> request);

}
