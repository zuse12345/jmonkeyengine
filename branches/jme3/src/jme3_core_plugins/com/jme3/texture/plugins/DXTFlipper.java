package com.jme3.texture.plugins;

import com.jme3.math.FastMath;
import com.jme3.texture.Image.Format;
import com.jme3.util.BufferUtils;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;

/**
 *
 * @author Kirill Vainer
 */
public class DXTFlipper {

    private static final ByteBuffer bb = ByteBuffer.allocate(8);
    private static final LongBuffer lb = bb.asLongBuffer();

    private static long readCode5(long data, int x, int y){
        long shift = (4 * y + x) * 3;
        long mask = 0x3 << shift;
        return (int) ((data >>> shift) & mask);
    }

    private static long writeCode5(long data, int x, int y, long code){
        long shift = (4 * y + x) * 3;
        long mask = 0x3 << shift;
        code = (code & 0x3) << shift;
        data &= ~0x3;
//        data &= ~mask; // remove prev code
//        data |= code; // write new code
        return data;
    }

    public static void flipDXT5Block(byte[] block, int h){
        bb.clear();
        bb.put(block, 2, 6).flip();
        lb.clear();
        long l = lb.get(0);
        lb.clear();

        for (int y = 0; y < 4; y++){
            for (int x = 0; x < 4; x++){
                int y2 = 4 - y - 1;
                long code = readCode5(l, x, y);
                l = writeCode5(l, x, y, code);
            }
        }

        lb.clear();
        lb.put(0, l).flip();
        bb.clear();
        bb.get(block, 2, 6).flip();
    }

    public static void flipDXT3Block(byte[] block, int h){
        // first row
        byte tmp0 = block[0];
        byte tmp1 = block[1];

        // write last row to first row
        block[0] = block[6];
        block[1] = block[7];

        // write first row to last row
        block[6] = tmp0;
        block[7] = tmp1;

        // 2nd row
        tmp0 = block[2];
        tmp1 = block[3];

        // write 3rd row to 2nd
        block[2] = block[4];
        block[3] = block[5];

        // write 2nd row to 3rd
        block[4] = tmp0;
        block[5] = tmp1;
    }

    /**
     * Flips a DXT color block or a DXT3 alpha block
     * @param block
     * @param h
     */
    public static void flipDXT1Block(byte[] block, int h){
        byte tmp;
        switch (h){
            case 1:
                return;
            case 2:
                // keep header intact (the two colors)
                // header takes 4 bytes

                // flip only two top rows
                tmp = block[4+1];
                block[4+1] = block[4+0];
                block[4+0] = tmp;
                return;
            default:
                // keep header intact (the two colors)
                // header takes 4 bytes

                // flip first & fourth row
                tmp = block[4+3];
                block[4+3] = block[4+0];
                block[4+0] = tmp;

                // flip second and third row
                tmp = block[4+2];
                block[4+2] = block[4+1];
                block[4+1] = tmp;
                return;
        }
    }

    public static ByteBuffer flipDXT(ByteBuffer img, int w, int h, Format format){
        int blocksX = (int) FastMath.ceil((float)w / 4f);
        int blocksY = (int) FastMath.ceil((float)h / 4f);

        int type;
        switch (format){
            case DXT1:
            case DXT1A:
                type = 1;
                break;
            case DXT3:
                type = 2;
                break;
            case DXT5:
                type = 3;
                break;
            default:
                throw new IllegalArgumentException();
        }

        // DXT1 uses 8 bytes per block,
        // DXT3 & DXT5 use 16 bytes per block
        int bpb = type == 1 ? 8 : 16;

        ByteBuffer retImg = BufferUtils.createByteBuffer(blocksX * blocksY * bpb);

        if (h == 1){
            retImg.put(img);
            retImg.rewind();
            return retImg;
        }else if (h == 2){
            byte[] colorBlock = new byte[8];
            byte[] alphaBlock = type != 1 ? new byte[8] : null;
            for (int x = 0; x < blocksX; x++){
                // prepeare for block reading
                int blockByteOffset = x * bpb;
                img.position(blockByteOffset);
                img.limit(blockByteOffset + bpb);

                img.get(colorBlock);
                flipDXT1Block(colorBlock, h);

                // write block (no need to flip block indexes, only pixels
                // inside block
                retImg.put(colorBlock);

                if (alphaBlock != null){
                    img.get(alphaBlock);
                    switch (type){
                        case 2: flipDXT3Block(alphaBlock, h); break;
                        case 3: flipDXT5Block(alphaBlock, h); break;
                    }
                    retImg.put(alphaBlock);
                }
            }
            retImg.rewind();
            return retImg;
        }else if (h >= 4){
            byte[] colorBlock = new byte[8];
            byte[] alphaBlock = type != 1 ? new byte[8] : null;
            for (int y = 0; y < blocksY; y++){
                for (int x = 0; x < blocksX; x++){
                    // prepeare for block reading
                    int blockIdx = y * blocksX + x;
                    int blockByteOffset = blockIdx * bpb;

                    img.position(blockByteOffset);
                    img.limit(blockByteOffset + bpb);

                    blockIdx = (blocksY - y - 1) * blocksX + x;
                    blockByteOffset = blockIdx * bpb;

                    retImg.position(blockByteOffset);
                    retImg.limit(blockByteOffset + bpb);

                    if (alphaBlock != null){
                        img.get(alphaBlock);
                        switch (type){
                            case 2: flipDXT3Block(alphaBlock, h); break;
                            case 3: flipDXT5Block(alphaBlock, h); break;
                        }
                        retImg.put(alphaBlock);
                    }

                    img.get(colorBlock);
                    flipDXT1Block(colorBlock, h);
                    retImg.put(colorBlock);
                }
            }
            retImg.limit(retImg.capacity());
            retImg.position(0);
            return retImg;
        }else{
            return null;
        }
    }

}
