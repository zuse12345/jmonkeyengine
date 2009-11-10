/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.g3d.scene;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 *
 * @author lex
 */
public class IndexByteBuffer extends IndexBuffer {

    private ByteBuffer buf;

    public IndexByteBuffer(ByteBuffer buffer) {
        this.buf = buffer;
    }
    
    @Override
    public int get(int i) {
        return buf.get(i) & 0x000000FF;
    }

    @Override
    public void put(int i, int value) {
        buf.put(i, (byte) value);
    }

    @Override
    public int size() {
        return buf.limit();
    }

    @Override
    public Buffer getBuffer() {
        return buf;
    }

}
