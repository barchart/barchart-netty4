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
 * Sorts URL patterns by number of path segments, whether or not they contain
 * embedded parameters, and by pattern length.
 *
 * Example: 2 handlers are defined as:
 *
 * <pre>
 * add("/service", handler1);
 * add("/service/info", handler2);
 * add("/service/{id}", handler3);
 * </pre>
 *
 * Routing for the following request URIs would be:
 *
 * <pre>
 * /service => handler1
 * /service/123 => handler 3 (dynamic parameters)
 * /service/info => handler2 (static URLs take priority over params)
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

	public RestEndpoint(final String template_) {

		template = template_;
		segments = template.split("/");
		params = new ArrayList<String>();
		isStatic = !PARAM_PATTERN.matcher(segments[segments.length - 1]).matches();

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
	 * The named parameters parsed from this URI template.
	 */
	public List<String> params() {
		return params;
	}

	/**
	 * Return a URI string for this endpoint by formatting it with the given
	 * parameters.
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
	 * Check if a URI matches this template.
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
	 * Parse a URI against this endpoint's template.
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