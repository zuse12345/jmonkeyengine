package com.jme3.network.message;

import com.jme3.network.serializing.Serializable;

/**
 * GZIPCompressedMessage is the class that you need to use should you want to
 *  compress a message using Gzip.
 *
 * @author Lars Wesselius
 */
@Serializable()
public class GZIPCompressedMessage extends CompressedMessage {
    public GZIPCompressedMessage() {
        super();
    }

    public GZIPCompressedMessage(Message msg) {
        super(msg);
    }
}
