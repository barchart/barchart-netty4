/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.client.base;

import io.netty.channel.ChannelPipeline;

import com.barchart.netty.client.facets.SecureFacet;
import com.barchart.netty.client.transport.TransportProtocol;
import com.barchart.netty.common.metadata.SecureAware;

/**
 * A Connectable client that negotiates SSL-TLS state on connect according to
 * the preferred connection security setting.
 */
public abstract class SecureConnectableBase<T extends SecureConnectableBase<T>>
		extends ConnectableBase<T> implements SecureAware {

	protected abstract static class Builder<B extends Builder<B, C>, C extends SecureConnectableBase<C>>
			extends ConnectableBase.Builder<B, C> {

		protected SecureAware.Encryption security =
				SecureAware.Encryption.REFUSE;

		@SuppressWarnings("unchecked")
		public B security(final SecureAware.Encryption security_) {
			security = security_;
			return (B) this;
		}

		@Override
		protected C configure(final C client) {
			super.configure(client);
			client.secureFacet = new SecureFacet(security);
			return client;
		}
	}

	protected SecureFacet secureFacet = null;

	protected SecureConnectableBase(final TransportProtocol transport_) {
		super(transport_);
	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {
		secureFacet.initPipeline(pipeline);
	}

	@Override
	public boolean secure() {
		return secureFacet.secure();
	}

}
