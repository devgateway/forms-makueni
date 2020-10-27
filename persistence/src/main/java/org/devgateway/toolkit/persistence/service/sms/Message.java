package org.devgateway.toolkit.persistence.service.sms;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Octavian Ciubotaru
 */
public class Message {

    @JsonProperty("Number")
    private final String number;

    @JsonProperty("Text")
    private final String text;

    public Message(String number, String text) {
        this.number = number;
        this.text = text;
    }

    public String getNumber() {
        return number;
    }

    public String getText() {
        return text;
    }
}
