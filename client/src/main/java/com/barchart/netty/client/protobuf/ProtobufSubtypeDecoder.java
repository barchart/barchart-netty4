package com.barchart.netty.client.protobuf;

import io.netty.buffer.MessageBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

@Sharable
public abstract class ProtobufSubtypeDecoder<P, T> extends
		MessageToMessageDecoder<P> {

	private final Map<T, MessageLite> decoders = new HashMap<T, MessageLite>();

	public ProtobufSubtypeDecoder(final Class<P> type) {
		super(type);
	}

	@Override
	protected void decode(final ChannelHandlerContext ctx, final P packet,
			final MessageBuf<Object> out) throws Exception {

		final Object type = getSubType(packet);
		if (decoders.containsKey(type)) {
			for (final ByteString body : getMessages(packet)) {
				out.add(decoders.get(type).getParserForType().parseFrom(body));
			}
		} else {
			out.add(packet);
		}

	}

	protected abstract T getSubType(P packet);

	protected abstract List<ByteString> getMessages(P packet);

	public void decodeAs(final T type, final MessageLite message) {
		decoders.put(type, message);
	}

}
