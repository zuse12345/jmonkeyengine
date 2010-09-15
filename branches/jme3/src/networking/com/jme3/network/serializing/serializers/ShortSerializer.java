package com.jme3.network.serializing.serializers;

import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Short serializer.
 *
 * @author Lars Wesselius
 */
public class ShortSerializer extends Serializer {
    public Short readObject(ByteBuffer data, Class c) throws IOException {
        return data.getShort();
    }

    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
        buffer.putShort((Short)object);
    }
}
