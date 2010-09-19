package com.jme3.network.message;

public class StreamMessage extends Message {
    private short streamID;

    public StreamMessage(short id) {
        streamID = id;
    }

    public StreamMessage() {}

    public short getStreamID() {
        return streamID;
    }

    public void setStreamID(short streamID) {
        this.streamID = streamID;
    }
}
