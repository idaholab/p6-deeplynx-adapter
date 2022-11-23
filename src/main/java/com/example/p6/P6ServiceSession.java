/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.p6;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.WSHandlerConstants;

public class P6ServiceSession {
	
	private static final Logger LOGGER = Logger.getLogger( P6ServiceSession.class.getName() );
	
    private String p6url;
    private String username;
    private String password;
    private int databaseInstanceId;
    private Map<String, Object> outProps = new HashMap<String, Object>();
    
    public P6ServiceSession(String uname, String pword, int databaseId, String url) {
        setP6url(url);
        username = uname;
        password = pword;
        databaseInstanceId = databaseId;        
        login();
    }
    
    private void login(){
        // Setting Username and Password
        outProps.put(WSHandlerConstants.ACTION,"UsernameToken Timestamp");
        outProps.put(WSHandlerConstants.USER, username);
        
        // Set password in System properties
        System.setProperty(ClientPasswordCallback.USERTOKEN_PASSWORD_KEY, password);
        outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        
        
        //Set the call client call back. This will set the password
        outProps.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientPasswordCallback.class.getName());
        outProps.put(WSHandlerConstants.USERNAME_TOKEN,ClientPasswordCallback.class.getName());
        
        //Sets the System Property for the database id
        System.setProperty(SoapHeaderHandler.DATABASE_INSTANCE_ID, Integer.toString(databaseInstanceId));

        LOGGER.log(Level.INFO, "Successful login"); 
    }
    
    public boolean setUserNameToken(BindingProvider port){
        // Implementing UserNameToken for Authentication. Token must be added to every web service call. 
        //Retrieve the client object from the port
        Client client = ClientProxy.getClient(port);
        Endpoint cxfEndpoint = client.getEndpoint();
         
        //Set the properties on the interceptor
        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
        cxfEndpoint.getOutInterceptors().add(wssOut);
        
        //Set the databaseInstanceId and add to the endpoint.
        SoapHeaderHandler intercepter = new SoapHeaderHandler();
        intercepter.setDatabaseInstanceId(databaseInstanceId);
        cxfEndpoint.getOutInterceptors().add(intercepter);

        return true;
    }

    public void setP6url(String p6url) {
        this.p6url = p6url;
    }

    public String getP6url() {
        return p6url;
    }
}

