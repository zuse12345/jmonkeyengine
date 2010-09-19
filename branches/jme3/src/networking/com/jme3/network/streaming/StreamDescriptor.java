package com.jme3.network.streaming;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Stream descriptor is a helper class for clients/servers that receive streams. This class
 *  will help the dev with choosing what to do with the stream, and execute it as such.
 *
 * @author Lars Wesselius
 */
public abstract class StreamDescriptor {
    protected byte[] data;

    StreamDescriptor(byte[] data) { this.data = data; } 

    public void toFile(File file) {
        try {
            FileOutputStream stream = new FileOutputStream(file);

            stream.write(data);
            stream.flush();
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ByteBuffer toByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    public InputStream toInputStream() {
        return new ByteArrayInputStream(data);
    }
    
    public byte[] toByteArray() {
        return data;
    }

    public String toString() {
        return "StreamDescriptor[data=" + data + "]";
    }

    public int getDataLength() {
        return data.length;
    }
}
