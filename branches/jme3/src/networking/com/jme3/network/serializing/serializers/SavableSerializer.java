package com.jme3.network.serializing.serializers;

import com.jme3.export.Savable;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.export.binary.BinaryImporter;
import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class SavableSerializer extends Serializer {

    private BinaryExporter exporter = new BinaryExporter();
    private BinaryImporter importer = new BinaryImporter();

    private static class BufferOutputStream extends OutputStream {

        ByteBuffer output;

        public BufferOutputStream(ByteBuffer output){
            this.output = output;
        }

        @Override
        public void write(int b) throws IOException {
            output.put( (byte) b );
        }

        @Override
        public void write(byte[] b){
            output.put(b);
        }

        @Override
        public void write(byte[] b, int off, int len){
            output.put(b, off, len);
        }
    }

    private static class BufferInputStream extends InputStream {

        ByteBuffer input;

        public BufferInputStream(ByteBuffer input){
            this.input = input;
        }

        @Override
        public int read() throws IOException {
            if (input.remaining() == 0)
                return -1;
            else
                return input.get() & 0xff;
        }

        @Override
        public int read(byte[] b){
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len){
            int toRead = len > input.remaining() ? input.remaining() : len;
            input.get(b, off, len);
            return toRead;
        }

    }

    @Override
    public <T> T readObject(ByteBuffer data, Class<T> c) throws IOException {
        BufferInputStream in = new BufferInputStream(data);
        Savable s = importer.load(in);
        in.close();
        return (T) s;
    }

    @Override
    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
        Savable s = (Savable) object;
        BufferOutputStream out = new BufferOutputStream(buffer);
        exporter.save(s, out);
        out.close();
    }

}
