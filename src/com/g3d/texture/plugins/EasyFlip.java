package com.g3d.texture.plugins;

import com.g3d.util.BufferUtils;
import java.nio.ByteBuffer;

public final class EasyFlip {

    public static final byte[] flipImage(byte[] data, int scanlineSize, int height) {
        byte[] newData = new byte[data.length];

        for (int y = 0; y < height; y++) {
            System.arraycopy(data, y * scanlineSize,
                    newData, (height - y - 1) * scanlineSize,
                    scanlineSize);
        }

        return newData;
    }

    public static final void flipImage(byte[] src, ByteBuffer dst, int width, int height, int bpp){
        int scanline = (bpp / 8) * width;
        for (int y = 0; y < height; y++){
            dst.put(src, (height - y - 1) * scanline, scanline);
        }
    }

    public static final void flipImage(ByteBuffer src, ByteBuffer dst, int width, int height, int bpp){
        int scanline = (bpp / 8) * width;
        ByteBuffer tmp = BufferUtils.createByteBuffer(scanline);
        for (int y = 0; y < height; y++){
            // read from src to tmp
            tmp.put(src);
            tmp.flip();

            // find opposite Y
            int oppositeY = height - y - 1;

            dst.position(oppositeY * scanline);
            dst.limit(scanline);
            dst.put(tmp);
            
            src.clear();
            dst.clear();
        }
    }

}
