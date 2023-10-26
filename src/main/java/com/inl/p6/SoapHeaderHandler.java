package com.inl.p6;

import javax.xml.namespace.QName;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.cxf.binding.soap.interceptor.SoapHeaderOutFilterInterceptor;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;

import org.w3c.dom.Document;

public class SoapHeaderHandler extends SoapHeaderOutFilterInterceptor {
    static final String DATABASE_INSTANCE_ID = "ws.auth.db.id";
    protected int databaseInstanceId;

    public SoapHeaderHandler() {
        super();
    }


    public void handleMessage(SoapMessage message) throws Fault {


        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();

        DocumentBuilder docBuilder = null;

        try {
            docBuilder = dbfac.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Document respDoc = docBuilder.newDocument();

        org.w3c.dom.Element dbElement =
            respDoc.createElementNS("http://xmlns.oracle.com/Primavera/P6/WS/Authentication/V1", "DatabaseInstanceId");
            // respDoc.createElementNS("http://xmlns.oracle.com/Primavera/P6/V7/WS/Authentication", "DatabaseInstanceId");

        dbElement.setTextContent(Integer.toString(databaseInstanceId));

        Header e = new Header(new QName("http://xmlns.oracle.com/Primavera/P6/WS/Authentication/V1", "AuthenticationService"), dbElement);
        // Header e = new Header(new QName("http://xmlns.oracle.com/Primavera/P6/V7/WS", "Authentication"), dbElement);

        message.getHeaders().add(e);

    }

    public void setDatabaseInstanceId(int databaseInstanceId) {
        this.databaseInstanceId = databaseInstanceId;
    }

    public int getDatabaseInstanceId() {
        return databaseInstanceId;
    }
}
