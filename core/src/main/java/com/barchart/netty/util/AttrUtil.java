package com.barchart.netty.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;

public final class AttrUtil {

	/**
	 * Get a context attribute, falling back to checking the associated channel
	 * attributes (and parent channel attributes, if a parent exists.)
	 */
	public static <T> T get(final AttributeKey<T> key,
			final ChannelHandlerContext ctx) {

		final T val = attr(key, ctx);

		if (val != null) {
			return val;
		}

		return get(key, ctx.channel());
	}

	/**
	 * Get a channel attribute, falling back to checking the parent channel
	 * attributes, if a parent exists.
	 */
	public static <T> T get(final AttributeKey<T> key, final Channel channel) {

		final T val = attr(key, channel);

		if (val != null) {
			return val;
		}

		if (channel.parent() != null) {
			return attr(key, channel.parent());
		}

		return null;

	}

	private static <T> T attr(final AttributeKey<T> key,
			final AttributeMap attMap) {

		return attMap.attr(key).get();

	}

}
