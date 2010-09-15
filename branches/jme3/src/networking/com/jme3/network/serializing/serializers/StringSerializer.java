package com.jme3.network.serializing.serializers;

import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/**
 * String serializer.
 *
 * @author Lars Wesselius
 */
public class StringSerializer extends Serializer {
    public String readObject(ByteBuffer data, Class c) throws IOException {

        int length = -1;
        byte type = data.get();
        if (type == (byte)0) {
            return null;
        } else if (type == (byte)1) {
            // Byte
            length = data.get();
        } else if (type == (byte)2) {
            // Short
            length = data.getShort();
        } else if (type == (byte)3) {
            // Int
            length = data.getInt();
        }
        if (length == -1) throw new IOException("Could not read String: Invalid length identifier.");

        byte[] buffer = new byte[length];
        data.get(buffer);
        return new String(buffer, "UTF-8");
    }

    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
        String string = (String)object;

        if (string == null) {
            // Write that it's 0.
            buffer.put((byte)0);
            return;
        }
        byte[] stringBytes = string.getBytes("UTF-8");
        int bufferLength = stringBytes.length;

        try {
            if (bufferLength <= Byte.MAX_VALUE) {
                buffer.put((byte)1);
                buffer.put((byte)bufferLength);
            } else if (bufferLength <= Short.MAX_VALUE) {
                buffer.put((byte)2);
                buffer.putShort((short)bufferLength);
            } else {
                buffer.put((byte)3);
                buffer.putInt(bufferLength);
            }
            buffer.put(stringBytes);
        }
        catch (BufferOverflowException e) {
            e.printStackTrace();
        }
    }
}
