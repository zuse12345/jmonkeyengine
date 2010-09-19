package com.jme3.network.message;

import com.jme3.network.serializing.Serializable;

/**
 * Stream message contains the data for the stream.
 *
 * @author Lars Wesselius
 */
@Serializable()
public class StreamDataMessage extends StreamMessage {
    private byte[] data;

    public StreamDataMessage() { }

    public StreamDataMessage(short id) {
        super(id);
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() { return data; }

}
