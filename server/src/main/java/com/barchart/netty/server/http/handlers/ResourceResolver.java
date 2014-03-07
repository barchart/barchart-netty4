package com.barchart.netty.server.http.handlers;


public interface ResourceResolver {

	Resource resolve(String path) throws ResourceNotFoundException;

}