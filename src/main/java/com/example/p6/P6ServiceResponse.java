package com.example.p6;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "P6v70WSResponse", propOrder = {
    "status",
    "msg",
    "returnObject"
})
public class P6ServiceResponse {

    @XmlElement(required = true)
    protected String status;
    @XmlElement(required = true)
    protected String msg;
    @XmlElement()
    protected byte[] returnObject;
    /**
     * Gets the value of the status property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setStatus(String value) {
        this.status = value;
    }

    /**
     * Gets the value of the msg property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMsg() {
        return msg;
    }

    /**
     * Sets the value of the msg property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMsg(String value) {
        this.msg = value;
    }

    public void setReturnObject(byte[] returnObject) {
        this.returnObject = returnObject;
    }

    public byte[] getReturnObject() {
        return returnObject;
    }
}
