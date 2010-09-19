package com.jme3.network.streaming;

/**
 * DataStreamDescriptor implements method specific to data streams sent
 *  across the network.
 *
 * @author Lars Wesselius
 */
public class DataStreamDescriptor extends StreamDescriptor {
    DataStreamDescriptor(byte[] data) {
        super(data);
    }
}
