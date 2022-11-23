/*
 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.p6;

import java.util.Date;

/**
 *
 * @author BOUNVL
 */

public class P6ServiceMessage {

    public enum MessageType {ACTIVITYCODE, RELATIONSHIPCODE, UDF, RESOURCE, APPLICTION, PROJECT}
    private MessageType type;
    private String message;
    private Date dateTime;

    public P6ServiceMessage() {
    }

    public void setType(P6ServiceMessage.MessageType type) {
        this.type = type;
    }

    public P6ServiceMessage.MessageType getType() {
        return type;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public Date getDateTime() {
        return dateTime;
    }
}
