package com.barchart.netty.guice.http;

import io.netty.handler.codec.http.HttpHeaders;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.barchart.netty.server.http.logging.RequestLogger;
import com.barchart.netty.server.http.request.HttpServerRequest;

public class SLF4JRequestLogger implements RequestLogger {

	Logger requestLogger = null;
	Logger errorLogger = null;

	InetAddress localhost;
	final DateTimeFormatter httpFormat = DateTimeFormat.forPattern("dd/MMM/yyyy:HH:mm:ss Z");
	final DateTimeFormatter iso8601Format = ISODateTimeFormat.dateTime();

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

	}

	@Override
	public void access(final HttpServerRequest request, final long duration) {

		if (requestLogger != null) {

			final StringBuilder sb = new StringBuilder();

			for (final String name : request.headers().names()) {
				requestLogger.info(name + ": " + request.headers().get(name));
			}

			final String forwardedFor = request.headers().get("X-Forwarded-For");
			if (forwardedFor != null && !forwardedFor.isEmpty()) {
				MDC.put("clientip", forwardedFor);
				sb.append(forwardedFor).append(" ");
			} else {
				final String remoteAddress = request.getRemoteAddress().getHostString();
				MDC.put("clientip", remoteAddress);
				sb.append(remoteAddress).append(" ");
			}

			final String remoteUserIdent = "-";
			MDC.put("ident", remoteUserIdent);
			sb.append(remoteUserIdent).append(" ");

			final String remoteUser = dashIfNull(request.getRemoteUser());
			MDC.put("user", remoteUser);
			sb.append(remoteUser).append(" ");

			final DateTime timestamp = new DateTime();
			MDC.put("timestamp", iso8601Format.print(timestamp));
			sb.append("[").append(httpFormat.print(timestamp)).append("] ");

			final String method = request.getMethod().toString();
			final String protocol = request.getProtocolVersion().toString();
			final String uri = request.getUri();
			MDC.put("method", method);
			MDC.put("protocol", protocol);
			MDC.put("request", uri);
			sb.append("\"").append(method).append(" ").append(uri).append(" ").append(protocol).append("\" ");

			final int statusCode = request.response().getStatus().code();
			MDC.put("status", Integer.toString(statusCode));
			sb.append(statusCode).append(" ");

			final long contentLength = request.response().writtenBytes();
			MDC.put("bytes", Long.toString(contentLength));
			sb.append(contentLength).append(" ");

			sb.append(duration).append(" ");
			MDC.put("duration", Long.toString(duration));

			final String referrer = dashIfNull(request.headers().get(HttpHeaders.Names.REFERER));
			MDC.put("referrer", referrer);
			sb.append("\"").append(referrer).append("\" ");

			final String userAgent = dashIfNull(request.headers().get(HttpHeaders.Names.USER_AGENT));
			MDC.put("agent", userAgent);
			sb.append("\"").append(userAgent).append("\" ");

			final String serverHost = dashIfNull(request.getServerHost());
			MDC.put("serverhost", serverHost);
			sb.append(serverHost).append(" ");

			final String serverAddress = request.getServerAddress().getHostString();
			MDC.put("serverip", serverAddress);
			sb.append(serverAddress);

			requestLogger.info(sb.toString());
			MDC.clear();

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
