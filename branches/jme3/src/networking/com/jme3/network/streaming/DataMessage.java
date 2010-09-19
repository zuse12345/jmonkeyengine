package com.jme3.network.streaming;

import com.jme3.network.message.StreamMessage;
import com.jme3.network.serializing.Serializable;

/**
 * A data message is the ending message of a data stream.
 *
 * @author Lars Wesselius
 */
@Serializable()
public class DataMessage extends StreamMessage {

    public DataMessage(short id) {
        super(id);
    }

    public DataMessage() {
    }
}
