package com.jme3.network.serializing.serializers;

import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Double serializer.
 *
 * @author Lars Wesselius
 */
public class DoubleSerializer extends Serializer {
    public Double readObject(ByteBuffer data, Class c) throws IOException {
        return data.getDouble();
    }

    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
        buffer.putDouble((Double)object);
    }
}
