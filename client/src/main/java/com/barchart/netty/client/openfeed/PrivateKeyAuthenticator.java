package com.barchart.netty.client.openfeed;

import java.net.URI;

import rx.Observable;

import com.barchart.account.api.Account;
import com.barchart.account.api.AuthResult;
import com.barchart.netty.client.facets.AuthenticationAware;
import com.barchart.netty.client.facets.AuthenticationAware.MessageStream;
import com.barchart.util.common.crypto.KerberosUtilities;

public class PrivateKeyAuthenticator implements
		AuthenticationAware.Authenticator<Account> {

	private final URI account;
	private final byte[] key;

	public PrivateKeyAuthenticator(final URI account_, final char[] secret_) {

		account = account_;
		key =
				KerberosUtilities.defaultSecretKey(account_.toString(),
						new String(secret_));

	}

	public PrivateKeyAuthenticator(final URI account_, final byte[] secretKey_) {

		account = account_;
		key = secretKey_;

	}

	@Override
	public Observable<AuthResult<Account>> authenticate(
			final MessageStream stream) {

		return Observable.error(new UnsupportedOperationException("TODO"));

	}

}
