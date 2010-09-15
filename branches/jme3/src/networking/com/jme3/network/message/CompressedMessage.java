package com.jme3.network.message;

import com.jme3.network.serializing.Serializable;

/**
 * CompressedMessage is a base class for all messages that
 *  compress others.
 *
 * @author Lars Wesselius
 */
@Serializable()
public class CompressedMessage extends Message {
    private Message message;

    public CompressedMessage() { }

    public CompressedMessage(Message msg) {
        this.message = msg;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
