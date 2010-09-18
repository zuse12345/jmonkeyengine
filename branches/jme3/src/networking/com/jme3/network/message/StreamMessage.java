package com.jme3.network.message;

import com.jme3.network.serializing.Serializable;

/**
 * Stream message contains the data for the stream.
 *
 * @author Lars Wesselius
 */
@Serializable()
public class StreamMessage extends Message {
    private byte[] data;
    private short id;

    public StreamMessage() { }

    public StreamMessage(short id) {
        this.id = id;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() { return data; }

    public void setID(short id) {
        this.id = id;
    }

    public short getID() { return id; }
}
