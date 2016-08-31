/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.rest.client.credentials;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import javax.xml.bind.DatatypeConverter;

import com.barchart.netty.rest.client.Credentials;
import com.barchart.netty.rest.client.RestRequest;

public class UsernamePasswordCredentials implements Credentials {

	private final static Charset UTF8 = Charset.forName("UTF-8");

	private final String username;
	private final char[] password;

	public UsernamePasswordCredentials(final String username_, final char[] password_) {
		username = username_;
		password = password_;
	}

	@Override
	public void authenticate(final RestRequest request) {

		byte[] firstBytes;

		try {
			firstBytes = (URLEncoder.encode(username, "UTF-8") + ":").getBytes(UTF8);
		} catch (final UnsupportedEncodingException e) {
			firstBytes = (username + ":").getBytes(UTF8);
		}

		final byte[] secondBytes = UTF8.encode(CharBuffer.wrap(password)).array();

		final byte[] concat = new byte[firstBytes.length + secondBytes.length];
		System.arraycopy(firstBytes, 0, concat, 0, firstBytes.length);
		System.arraycopy(secondBytes, 0, concat, firstBytes.length, secondBytes.length);

		request.header("Authorization", "Basic " + DatatypeConverter.printBase64Binary(concat));

	}
}
