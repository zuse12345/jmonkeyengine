package com.jme3.network.serializing.serializers;

import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Char serializer.
 *
 * @author Lars Wesselius
 */
public class CharSerializer extends Serializer {
    public Character readObject(ByteBuffer data, Class c) throws IOException {
        return data.getChar();
    }

    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
        buffer.putChar((Character)object);
    }
}
