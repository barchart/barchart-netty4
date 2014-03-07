package com.barchart.netty.rest.server;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a REST endpoint path template. This allows encapsulating path
 * parsing and generation into common endpoints shared by both client and
 * server.
 *
 * And endpoint pattern consists of a URI path with optional name parameter
 * placeholders.
 *
 * Example:
 *
 * <pre>
 * new RestEndpoint("/service");
 * new RestEndpoint("/service/info");
 * new RestEndpoint("/service/{id}");
 * new RestEndpoint("/service/{id}/status");
 * </pre>
 */
public class RestEndpoint implements Comparable<RestEndpoint> {

	public static final String ROOT = "";

	public static final String PARAM_START = "{";
	public static final String PARAM_END = "}";

	private static final Pattern PARAM_PATTERN = Pattern.compile("\\" + PARAM_START + "[^\\/]+\\" + PARAM_END);
	private static final String TARGET_PARAM = "([^\\/]+)";

	private final String[] segments;
	private final String template;
	private final String prefix;
	private final Pattern compiled;
	private final List<String> params;
	private final boolean isStatic;

	/**
	 * Create a new REST endpoint.
	 *
	 * URI templates are matched from the start of request URI. You can specify
	 * named parameters in the URI pattern, which will be parsed out and added
	 * to the ServerRequest.getParameters() values available to the REST
	 * handler.
	 *
	 * Trailing slashes are stripped when matching URIs, so the following
	 * patterns are equivalent:
	 *
	 * <pre>
	 * /account/create
	 * /account/create/
	 * </pre>
	 *
	 * Examples:
	 *
	 * <pre>
	 * new RestEndpoint("/account");
	 * new RestEndpoint("/account/create");
	 * new RestEndpoint("/account/{id}");
	 * new RestEndpoint("/account/{id}/orders");
	 * new RestEndpoint("/account/{id}/orders/{order}");
	 * </pre>
	 *
	 * A request to "/account/1234" would match the third template, and a call
	 * to parse() would return an "id" parameter equal to "1234".
	 *
	 * To match the root path, use "" or "/" as the pattern.
	 *
	 * @param template_ The URI template to match
	 */
	public RestEndpoint(final String template_) {

		template = template_;
		segments = template.split("/");
		params = new ArrayList<String>();
		isStatic = segments.length == 0 ? true : !PARAM_PATTERN.matcher(segments[segments.length - 1]).matches();

		if (template.contains(PARAM_START)) {

			// Has URI path params
			prefix = template.substring(0, template.indexOf(PARAM_START));

			final StringBuffer sb = new StringBuffer();
			final Matcher matcher = PARAM_PATTERN.matcher(template);
			while (matcher.find()) {
				// Find parameter name
				final String name = matcher.group();
				params.add(name.substring(1, name.length() - 1));
				// Replace with regex pattern for matching requests
				matcher.appendReplacement(sb, TARGET_PARAM);
			}
			matcher.appendTail(sb);

			compiled = Pattern.compile("^" + sb.toString());

		} else {

			// Static path, no params
			prefix = template;
			compiled = null;

		}

	}

	/**
	 * The URI template for this endpoint.
	 */
	public String template() {
		return template;
	}

	/**
	 * The named parameters that are present in this URI template (automatically
	 * parsed).
	 */
	public List<String> params() {
		return params;
	}

	/**
	 * Return a URI string for this endpoint by formatting it with the given
	 * parameters.
	 *
	 * For example, for the following endpoint template:
	 *
	 * <code>/service/{id}/status</code>
	 *
	 * Calling:
	 *
	 * <code>endpoint.format(new HashMap() {{ put("id", "123"); }})</code>
	 *
	 * Will return a formatted URI path of:
	 *
	 * <code>/service/123/status</code>
	 */
	public String format(final Map<String, String> values) {

		String formatted = template;

		for (final String p : params) {

			if (!values.containsKey(p))
				throw new IllegalArgumentException("Required parameter '" + p + "' not provided");

			try {
				formatted = formatted.replace(PARAM_START + p + PARAM_END,
						URLEncoder.encode(values.get(p), "UTF-8"));
			} catch (final UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}

		return formatted;

	}

	/**
	 * Check if a request URI matches this endpoint template.
	 *
	 * @return True if the given URI matches this endpoint's template
	 */
	public boolean match(final String uri) {

		if (compiled == null) {

			// Root handler is special, don't want it to match everything
			if (ROOT.equals(template)) {
				if (ROOT.equals(uri)) {
					return true;
				}
			} else if (uri.startsWith(template)) {
				return true;
			}

		} else if (uri.startsWith(prefix)) {

			return compiled.matcher(uri).find();

		}

		return false;

	}

	/**
	 * Parse a URI against this endpoint's template. You should first verify a
	 * URI with match() to avoid exceptions.
	 *
	 * @return A parsed representation of the URI, or null if it does not match
	 * @throws ParseException If the URI could not be matched
	 */
	public Parsed parse(final String uri) throws ParseException {

		if (match(uri)) {

			if (compiled == null) {

				return new Parsed(null, uri, template);

			} else {

				final Matcher matcher = compiled.matcher(uri);

				if (matcher.find()) {

					final Map<String, String> uriParams =
							new HashMap<String, String>();

					for (int i = 0; i < matcher.groupCount(); i++) {
						try {
							uriParams.put(params.get(i), URLDecoder.decode(
									matcher.group(i + 1), "UTF-8"));
						} catch (final UnsupportedEncodingException e) {
							throw new RuntimeException(e);
						}
					}

					return new Parsed(uriParams, uri, matcher.group());

				}

			}

		}

		throw new ParseException("URI does not match the template '" + template + "'", 0);

	}

	/**
	 * Compare to another RestEndpoint. Sort ordering follows the following
	 * rules in the given order:
	 *
	 * 1. The endpoint with the most path segments comes first to ensure more
	 * detailed URL templates get matched first
	 *
	 * 2. If the endpoints have the same number of segments, any endpoint
	 * without named parameters comes first
	 *
	 * 3. If both endpoint have parameters (or don't), order based on pattern
	 * length
	 *
	 * 4. If pattern lengths are the same, fallback to String.compareTo()
	 */
	@Override
	public int compareTo(final RestEndpoint o) {

		// Patterns with more path segments should be matched first
		if (segments.length < o.segments.length) {
			return 1;
		} else if (segments.length > o.segments.length) {
			return -1;
		}

		// Patterns with same number of segments should match static paths
		// first
		if (isStatic && !o.isStatic) {
			return -1;
		} else if (!isStatic && o.isStatic) {
			return 1;
		}

		// Finally compare pattern length if everything matches
		final int l1 = template.length();
		final int l2 = o.template.length();

		if (l1 < l2) {
			return 1;
		} else if (l2 < l1) {
			return -1;
		}

		return template.compareTo(o.template);

	}

	@Override
	public String toString() {
		if (template != null) {
			return template;
		}
		return "";
	}

	@Override
	public boolean equals(final Object o) {
		if (template == null) {
			return o == null;
		}
		return template.equals(o.toString());
	}

	@Override
	public int hashCode() {
		if (template != null) {
			return template.hashCode();
		}
		return 0;
	}

	/**
	 * An URI template match with parsed parameter values
	 */
	public static class Parsed {

		private final Map<String, String> params;
		private final String uri;
		private final String match;

		protected Parsed(final Map<String, String> params_, final String uri_, final String match_) {
			params = params_;
			match = match_;
			uri = uri_;
		}

		/**
		 * The parameter values parsed from the URI.
		 */
		public Map<String, String> params() {
			return params;
		}

		/**
		 * The matching URI.
		 */
		public String uri() {
			return uri;
		}

		/**
		 * The section of the URI that matched the REST endpoint template.
		 */
		public String match() {
			return match;
		}

		/**
		 * The extra path info after the matched section of the URI.
		 */
		public String path() {
			return uri.substring(match.length());
		}

	}

}