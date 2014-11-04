/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.client.example;

import io.netty.channel.ChannelHandlerAdapter;
import rx.Observable;

import com.barchart.netty.client.pipeline.AuthenticationHandler;
import com.barchart.netty.common.metadata.AuthenticationAware;

public class DummyAuthenticationHandler extends ChannelHandlerAdapter implements
		AuthenticationHandler<Object> {

	public static class Builder implements
			AuthenticationHandler.Builder<Object> {

		@Override
		public AuthenticationHandler<Object> build() {
			return new DummyAuthenticationHandler();
		}

	}

	@Override
	public Observable<AuthenticationAware.AuthState> authStateChanges() {
		return Observable.just(AuthenticationAware.AuthState.NOT_AUTHENTICATED);
	}

	@Override
	public AuthenticationAware.AuthState authState() {
		return AuthenticationAware.AuthState.NOT_AUTHENTICATED;
	}

	@Override
	public Object account() {
		return null;
	}

}