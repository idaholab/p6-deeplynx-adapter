/*
 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.inl.de.p6adapter;

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