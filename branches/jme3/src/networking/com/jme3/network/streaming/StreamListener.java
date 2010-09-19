package com.jme3.network.streaming;

/**
 * Used for file sending/receiving.
 *
 * @author Lars Wesselius
 */
public interface StreamListener {
    public void streamReceived(StreamDescriptor descriptor);
}
