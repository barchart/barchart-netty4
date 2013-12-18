package com.barchart.netty.client.example;

import io.netty.channel.ChannelHandlerAdapter;
import rx.Observable;

import com.barchart.netty.client.facets.AuthenticationAware;
import com.barchart.netty.client.pipeline.AuthenticationHandler;

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