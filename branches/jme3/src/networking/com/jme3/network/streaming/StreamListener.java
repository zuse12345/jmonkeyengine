package com.jme3.network.streaming;

import com.jme3.network.message.StreamDataMessage;
import com.jme3.network.message.StreamMessage;

/**
 * Used for stream sending/receiving.
 *
 * @author Lars Wesselius
 */
public interface StreamListener {
    public boolean streamOffered(StreamMessage message);
    public void streamDataReceived(StreamDataMessage message);
    public void streamCompleted(StreamMessage message);
}
