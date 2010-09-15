package com.jme3.network.serializing.serializers;

import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Boolean serializer.
 *
 * @author Lars Wesselius
 */
public class BooleanSerializer extends Serializer {

    public Boolean readObject(ByteBuffer data, Class c) throws IOException {
        return data.get() == 1;
    }

    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
        buffer.put(((Boolean)object) ? (byte)1 : (byte)0);
    }
}
