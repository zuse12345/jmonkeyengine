package com.jme3.network.serializing.serializers;

import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Serializes uses Java built-in method.
 *
 * TODO
 * @author Lars Wesselius
 */
public class SerializableSerializer extends Serializer {
    public Serializable readObject(ByteBuffer data, Class c) throws IOException {
        return null;
    }

    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
    }
}
