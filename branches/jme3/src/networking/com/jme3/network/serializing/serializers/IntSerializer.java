package com.jme3.network.serializing.serializers;

import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The Integer serializer serializes...integers. Big surprise.
 *
 * @author Lars Wesselius
 */
public class IntSerializer extends Serializer {
    public Integer readObject(ByteBuffer data, Class c) throws IOException {
        return data.getInt();
    }

    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
        buffer.putInt((Integer)object);
    }
}
