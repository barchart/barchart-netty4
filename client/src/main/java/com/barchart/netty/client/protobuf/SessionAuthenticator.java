package com.barchart.netty.client.protobuf;

import rx.Observable;

import com.barchart.account.api.Account;
import com.barchart.account.api.AuthResult;
import com.barchart.netty.client.facets.AuthenticationAware;
import com.barchart.netty.client.facets.AuthenticationAware.MessageStream;

public class SessionAuthenticator implements
		AuthenticationAware.Authenticator<Account> {

	public SessionAuthenticator(final String username_, final String password_) {
	}

	@Override
	public Observable<AuthResult<Account>> authenticate(
			final MessageStream stream) {

		return Observable.error(new UnsupportedOperationException("TODO"));

	}

}
