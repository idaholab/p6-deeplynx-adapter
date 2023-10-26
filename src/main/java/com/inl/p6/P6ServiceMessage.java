package com.inl.p6;

import java.util.Date;

/**
 *
 * @author BOUNVL
 */

public class P6ServiceMessage {

    public enum MessageType {ACTIVITYCODE, RELATIONSHIPCODE, UDF, RESOURCE, APPLICATION, PROJECT}
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
