package com.barchart.netty.server.http.handlers;

import java.io.IOException;
import java.io.InputStream;

public interface Resource {

	long modified();

	long size();

	String contentType();

	InputStream stream() throws IOException;

}