package com.barchart.netty.guice.http;

import io.netty.handler.codec.http.HttpHeaders;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.server.http.logging.RequestLogger;
import com.barchart.netty.server.http.request.HttpServerRequest;

public class SLF4JRequestLogger implements RequestLogger {

	Logger requestLogger = null;
	Logger errorLogger = null;

	InetAddress localhost;
	SimpleDateFormat dateFormatter;

	public SLF4JRequestLogger(final String request, final String error) {

		if (request != null) {
			requestLogger = LoggerFactory.getLogger(request);
		}

		if (error != null) {
			errorLogger = LoggerFactory.getLogger(error);
		}

		try {
			localhost = InetAddress.getLocalHost();
		} catch (final UnknownHostException e) {
			localhost = InetAddress.getLoopbackAddress();
		}

		dateFormatter = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");

	}

	@Override
	public void access(final HttpServerRequest request, final long duration) {

		if (requestLogger != null) {

			final StringBuilder sb = new StringBuilder();
			// Client address
			sb.append(request.getRemoteAddress().getHostString()).append(" ");
			// User ident
			sb.append("- ");
			// Username
			sb.append(dashIfNull(request.getRemoteUser())).append(" ");
			// Date
			sb.append("[").append(dateFormatter.format(new Date()))
					.append("] ");
			// Request URL
			sb.append("\"").append(request.getMethod().toString()).append(" ")
					.append(request.getUri()).append(" ")
					.append(request.getProtocolVersion().toString())
					.append("\" ");
			// Status code
			sb.append(request.response().getStatus().code()).append(" ");
			// Content length
			sb.append(request.response().writtenBytes()).append(" ");
			// Request duration
			sb.append(duration).append(" ");
			// Referrer
			sb.append("\"")
					.append(dashIfNull(request.headers().get(
							HttpHeaders.Names.REFERER))).append("\" ");
			// User-agent
			sb.append("\"")
					.append(dashIfNull(request.headers().get(
							HttpHeaders.Names.USER_AGENT))).append("\" ");
			// Host header
			sb.append(dashIfNull(request.getServerHost())).append(" ");
			// Host address
			sb.append(request.getServerAddress().getHostString());

			requestLogger.info(sb.toString());

		}

	}

	private String dashIfNull(final String value) {
		if (value == null) {
			return "-";
		}
		return value;
	}

	@Override
	public void error(final HttpServerRequest request, final Throwable exception) {
		if (errorLogger != null) {
			errorLogger.warn(exception.getMessage());
		}
	}

}
