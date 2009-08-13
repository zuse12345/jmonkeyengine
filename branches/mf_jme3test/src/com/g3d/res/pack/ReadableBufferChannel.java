package com.g3d.res.pack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;

public class ReadableBufferChannel implements ReadableByteChannel {

    private ByteBuffer bb;

    public ReadableBufferChannel(ByteBuffer bb){
        if (bb == null)
            throw new NullPointerException();

        this.bb = bb;
    }

    public int read(ByteBuffer dst) throws IOException {
        if (bb == null)
            throw new ClosedChannelException();

        int toRead = Math.min(dst.remaining(), bb.remaining());
        int prevLim = bb.limit();
        int newLim = bb.position() + toRead;
        assert newLim <= prevLim;
        bb.limit(newLim);
        assert bb.remaining() == toRead;
        dst.put(bb);
        return toRead;
    }

    public boolean isOpen() {
        return bb != null;
    }

    public void close() throws IOException {
        bb = null;
    }



}
