package com.barchart.netty.server.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.barchart.netty.common.messages.Capabilities;
import com.barchart.netty.common.messages.Version;
import com.barchart.netty.common.messages.VersionRequest;
import com.barchart.netty.common.messages.VersionResponse;
import com.barchart.netty.common.metadata.VersionAware;

/**
 * Channel handler that immediately writes a Capabilities message to the client
 * on connect.
 */
public class CapabilitiesHandler extends
		SimpleChannelInboundHandler<VersionRequest> implements VersionAware {

	private final Version version;
	private final Version minVersion;
	private final Set<String> capabilities;

	private Version activeVersion;

	public CapabilitiesHandler(final Version version_,
			final Version minVersion_, final String... capabilities_) {

		version = version_;
		minVersion = minVersion_;
		capabilities = new HashSet<String>(Arrays.asList(capabilities_));

	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {

		ctx.writeAndFlush(new Capabilities() {

			@Override
			public Set<String> capabilities() {
				return capabilities;
			}

			@Override
			public Version version() {
				return version;
			}

			@Override
			public Version minVersion() {
				return minVersion;
			}

		});

	}

	@Override
	protected void channelRead0(final ChannelHandlerContext ctx,
			final VersionRequest request) throws Exception {

		final Version v = request.version();

		if (minVersion.lessThanOrEqual(v) && version.greaterThanOrEqual(v)) {

			activeVersion = v;

			ctx.writeAndFlush(new VersionResponse() {

				@Override
				public boolean success() {
					return true;
				}

				@Override
				public Version version() {
					return v;
				}

			});

		} else {

			ctx.writeAndFlush(new VersionResponse() {

				@Override
				public boolean success() {
					return false;
				}

				@Override
				public Version version() {
					return version;
				}

			});

		}

	}

	@Override
	public Version version() {
		return activeVersion;
	}

}
