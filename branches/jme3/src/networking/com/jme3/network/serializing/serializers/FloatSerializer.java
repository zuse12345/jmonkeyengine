package com.jme3.network.serializing.serializers;

import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Float serializer.
 *
 * @author Lars Wesselius
 */
public class FloatSerializer extends Serializer {
    public Float readObject(ByteBuffer data, Class c) throws IOException {
        return data.getFloat();
    }

    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
        buffer.putFloat((Float)object);
    }
}
