package com.barchart.netty.client.facets;

import io.netty.channel.ChannelPipeline;

import com.barchart.netty.client.pipeline.CapabilitiesRequest;
import com.barchart.netty.client.pipeline.SecureFlowHandler;
import com.barchart.netty.common.metadata.SecureAware;

/**
 * Connectable proxy facet that implements the SecureAware interface and
 * associated functionality.
 */
public class SecureFacet implements ConnectableFacet<SecureAware>, SecureAware {

	private SecureFlowHandler handler = null;

	private final SecureAware.Encryption security;

	public SecureFacet(final SecureAware.Encryption security_) {
		security = security_;
	}

	@Override
	public Class<SecureAware> type() {
		return SecureAware.class;
	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {

		switch (security) {

			case REFUSE:
				handler = null;
				break;

			case REQUIRE:
				handler = new SecureFlowHandler(true);
				break;

			case OPTIONAL:
			default:
				handler = new SecureFlowHandler(false);

		}

		if (handler != null) {

			if (pipeline.get(CapabilitiesRequest.class) == null)
				pipeline.addLast(new CapabilitiesRequest());

			pipeline.addLast(handler);

		}

	}

	@Override
	public boolean secure() {

		if (handler != null) {
			return handler.secure();
		}

		return false;

	}

}
