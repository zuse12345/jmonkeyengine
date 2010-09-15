package com.jme3.network.serializing.serializers;

import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Date serializer.
 *
 * @author Lars Wesselius
 */
public class DateSerializer extends Serializer {
    public Date readObject(ByteBuffer data, Class c) throws IOException {
        return new Date(data.getLong());
    }

    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
        buffer.putLong(((Date)object).getTime());
    }
}
