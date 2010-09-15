package com.jme3.network.serializing.serializers;

import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Enum serializer.
 *
 * @author Lars Wesselius
 */
public class EnumSerializer extends Serializer {
    public <T> T readObject(ByteBuffer data, Class<T> c) throws IOException {
        T[] enumConstants = c.getEnumConstants();
        return enumConstants[data.getInt()];
    }

    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
        buffer.putInt(((Enum)object).ordinal());
    }
}
