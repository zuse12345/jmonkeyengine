package com.g3d.res.pack;

import java.io.IOException;
import java.util.zip.Deflater;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;

public class DeflaterWritableChannel implements WritableByteChannel {

    private WritableByteChannel out;
    private Deflater d;
    private boolean closed = false;
    private ByteBuffer buf;
    private byte[] arr;

    public DeflaterWritableChannel(WritableByteChannel out, Deflater d, int bufSize){
        if (out == null || d == null)
            throw new NullPointerException();

        this.out = out;
        this.d = d;
        init(bufSize);
    }

    private void init(int bufSize){
        buf = ByteBuffer.allocate(bufSize);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        arr = buf.array();
    }

    private int writeFromArray(byte[] arr, int offset, int length) throws IOException{
        d.setInput(arr, offset, length);
        int wrote = 0;
        while (!d.needsInput()){
            // deflate into arr
            wrote += deflate();
        }
        return wrote;
    }

    public int write(ByteBuffer src) throws IOException {
        if (d.finished())
            throw new IllegalStateException();

        if (!src.hasRemaining())
            return 0;

        if (src.hasArray()){
            return writeFromArray(src.array(), src.arrayOffset(), src.remaining());
        }

        int prevLim = src.limit();
        int wrote = 0;
        while (src.hasRemaining()){
            buf.rewind();
            buf.limit(buf.capacity());
            int toCopy = Math.min(src.remaining(), buf.remaining());

            // copy 'toCopy' bytes from src to buf
            src.limit(src.position() + toCopy);
            buf.put(src);
            buf.flip();

            // this call copies the data from the array
            // into native memory so arr can be used freely
            // after this call.
            d.setInput(arr, 0, toCopy);

            while (!d.needsInput()){
                // deflate into arr
                wrote += deflate();
            }
            src.limit(prevLim);
        }
        
        return wrote;
    }

    private int deflate() throws IOException{
        int deflated = d.deflate(arr);
        if (deflated > 0){
            buf.rewind();
            buf.limit(deflated);
            return out.write(buf);
        }
        return 0;
    }
    
    public boolean isOpen() {
        return !closed;
    }

    public void finish() throws IOException{
        if (!d.finished()){
            d.finish();
            while (!d.finished()){
                deflate();
            }
        }
    }

    public void close() throws IOException {
        if (!closed){
            finish();
            out.close();
            closed = true;
        }
    }

}
