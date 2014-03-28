/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.rest.server.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.HttpHeaders;

import java.security.MessageDigest;

import org.apache.commons.io.IOUtils;

import com.barchart.netty.server.http.request.HttpServerRequest;
import com.barchart.util.common.crypto.KerberosUtilities;

public class SigningUtil {

	public static byte[] bytesToSign(final HttpServerRequest request)
			throws Exception {

		final ByteBuf content = request.getContent();
		content.markReaderIndex();
		final byte[] contentBytes =
				IOUtils.toByteArray(new ByteBufInputStream(content));
		content.resetReaderIndex();

		final String md5 =
				KerberosUtilities.bytesToHex(MessageDigest.getInstance("MD5")
						.digest(contentBytes));

		final StringBuilder sb = new StringBuilder();
		sb.append(request.getMethod().name())
				.append("\n")
				.append(request.getUri())
				.append("\n")
				.append(md5)
				.append("\n")
				.append(nullCheck(request.headers().get(
						HttpHeaders.Names.CONTENT_TYPE)))
				.append("\n")
				.append(nullCheck(request.headers().get(HttpHeaders.Names.DATE)))
				.append("\n");

		return sb.toString().getBytes("UTF-8");

	}

	private static String nullCheck(final String value) {
		if (value == null) {
			return "";
		}
		return value;
	}

}
