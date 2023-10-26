// Copyright 2023, Battelle Energy Alliance, LLC All Rights Reserved

package com.inl.p6;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;

public class ClientPasswordCallback implements CallbackHandler {
    public static final String USERTOKEN_PASSWORD_KEY = "p6.usertoken.password";

    public ClientPasswordCallback() {
        super();
    }

    public void handle(Callback[] callbacks) throws IOException,
                                                    UnsupportedCallbackException {
        WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];

        // Get password from session...
        pc.setPassword(System.getProperty(USERTOKEN_PASSWORD_KEY));
    }
}
