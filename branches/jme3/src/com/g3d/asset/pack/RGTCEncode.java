/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.g3d.asset.pack;

import com.g3d.util.BufferUtils;
import java.math.BigInteger;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.BitSet;

/**
 *
 * @author Kirill
 */
public class RGTCEncode {

    private static final int MinCode = 0,
                             MaxCode = 1;

    /* Note which group of codes were using
    ** in our encoding.
    **
    ** Group[0] ==> red_0 > red_1
    ** Group[1] ==> red_0 <= red_1
    */
    private static final int[] Group = { 0x00, 0x80 };
    private static final int[] Mask = { 0x07, 0x38 };
    private static final int[] Shift = { 0, 3 };

    public static void main(String[] args){
        int[] pixels =
        {
            0xff, 0xff, 0xff, 0xff,
            0xaa, 0xaa, 0xaa, 0xaa,
            0x55, 0x55, 0x55, 0x55,
            0x00, 0x00, 0x00, 0x00,
        };
        ByteBuffer store = BufferUtils.createByteBuffer(8);
        store.order(ByteOrder.LITTLE_ENDIAN);
        RGTCEncode(pixels, store);
        store.clear();
        long val = store.getLong(0);
        System.out.println("----");
        store.clear();     
        Arrays.fill(pixels, 0);
        RGTCDecode(store, pixels);
        for (int y = 0; y < 4; ++y){
            for (int x = 0; x < 4; ++x){
                System.out.print(Integer.toHexString(pixels[4*y+x]) + " ");
            }
            System.out.println();
        }
    }

    public static final void RGTCEncode(int[] pixels, ByteBuffer store){
        int min = 0xff, max = 0x00; /* range for current 4x4 block */
        int[] codes = new int[16];
        int group = 0;
        long bits;

        /* compute min & max pixel values */
        for ( int i = 0; i < 16; ++i ) {
            int val = pixels[i];
            if ( min > val ) min = val;
            if ( max < val ) max = val;
        }
        if ( min == max ) {
            /* Constant color across the block - we'll use the
            ** first set of codes (red_0 > red_1), and set
            ** all bits to be code 0 */
            store.clear();
            store.put( (byte) min);
            store.put( (byte) max);
            store.put( (byte) 0x00 );
            store.put( (byte) 0x00 );
            store.put( (byte) 0x00 );
            store.put( (byte) 0x00 );
            store.put( (byte) 0x00 );
            store.put( (byte) 0x00 );
            store.flip();
        } else {
            double d1 = (max - min) / 7.0f;
            double d2 = (max - min) / 5.0f;

            for ( int i = 0; i < 16; i++ ) {
                double v1 = (pixels[i] - min) / d1;
                double v2 = (pixels[i] - min) / d2;

                int ip1 = (int) v1;
                int ip2 = (int) v2;

                double fp1 = v1 - ip1;
                double fp2 = v2 - ip2;

                if ( fp1 > 0.5 ) {
                    fp1 -= 0.5;
                    ip1 += 1;
                }

                if ( fp2 > 0.5 ) {
                    fp2 -= 0.5;
                    ip1 += 1;
                }

                if ( ip1 == 0 )
                    codes[i] = MinCode;
                else if ( ip1 == 7 )
                    codes[i] = MaxCode;
                else
                    codes[i] = (ip2 << Shift[1]) | (ip1 << Shift[0]);

                if ( fp1 < fp2 ) {
                    /* The current pixel's closer to one of the
                    ** seventh range partitoning values */
                    codes[i] |= Group[0];
                    ++group;
                } else {
                    /* Here, the current pixel's closer to
                    ** one of the fifth range partitonings
                    ** values */
                    codes[i] |= Group[1];
                    --group;
                }
            }

            group = (group >= 0) ? 0 : 1;
            store.clear();
            bits = 0;
            for ( int y = 0; y < 4; ++y ) {
                for ( int x = 0; x < 4; ++x ) {
                    long shift = 16 + 3*(4*y+x);
                    long code;
                    long mask;

                    
                    code = codes[4*y+x] & Mask[group];
                    code >>= Shift[group];
                    System.out.print(code+" ");
                    mask = code << shift;
                    bits |= mask;
                }
                System.out.println();
            }
            store.putLong(0, bits);
            if ( group == 0 ) {
                store.put(0, (byte) max );
                store.put(1, (byte) min );
            }
            else {
                store.put(0, (byte) min );
                store.put(1, (byte) max );
            }
            store.clear();
        }
    }

    private static final int getCode(BitSet b, int x, int y){
        assert x >= 0 && y >=0 && x < 4 && y < 4;
        int bitNum = 16 + 3*(4*y+x);
        boolean b1 = b.get(bitNum);
        boolean b2 = b.get(bitNum+1);
        boolean b3 = b.get(bitNum+2);
        return (b1?1:0) + (b2?2:0) + (b3?4:0);
    }

    private static BitSet fromByteArray(byte[] bytes) {
        BitSet bits = new BitSet();
        for (int i=0; i<bytes.length*8; i++) {
            if ((bytes[bytes.length-i/8-1]&(1<<(i%8))) > 0) {
                bits.set(i);
            }
        }
        return bits;
    }

    public static final void RGTCDecode(ByteBuffer source, int[] pixels){
        int r0 = source.get(0) & 0xFF;
        int r1 = source.get(1) & 0xFF;
        int group = r0 > r1 ? 0 : 1;

        source.position(0).limit(8);
        long bits = source.getLong(0);
        int[] codes = new int[16];

        for ( int y = 0; y < 4; ++y ) {
            for ( int x = 0; x < 4; ++x ) {
                long shift = 16 + 3*(4*y+x);
                long code;
                long mask;

                mask = bits;
                code = mask >> shift;
                code = code & 0x07;
                codes[4*y+x] = (int) code;
            }
        }

        for (int y = 0; y < 4; ++y){
            for (int x = 0; x < 4; ++x){
                int i = y * 4 + x;
                int code = codes[i];
                double v = 0;
                if (group == 1){
                    switch (code){
                        case 0:
                            v = r0;
                            break;
                        case 1:
                            v = r1;
                            break;
                        case 2:
                            v = (6.0*r0+r1) / 7.0;
                            break;
                        case 3:
                            v = (5.0*r0+2.0*r1) / 7.0;
                            break;
                        case 4:
                            v = (4.0*r0+3.0*r1) / 7.0;
                            break;
                        case 5:
                            v = (3.0*r0+4.0*r1) / 7.0;
                            break;
                        case 6:
                            v = (2.0*r0+5.0*r1) / 7.0;
                            break;
                        case 7:
                            v = (r0+6.0*r1) / 7.0;
                            break;
                    }
                }else{
                    switch (code){
                        case 0:
                            v = r0;
                            break;
                        case 1:
                            v = r1;
                            break;
                        case 2:
                            v = (4.0*r0+r1) / 5.0;
                            break;
                        case 3:
                            v = (3.0*r0+2.0*r1) / 5.0;
                            break;
                        case 4:
                            v = (2.0*r0+3.0*r1) / 5.0;
                            break;
                        case 5:
                            v = (r0+4.0*r1) / 5.0;
                            break;
                        case 6:
                            v = 0.0;
                            break;
                        case 7:
                            v = 1.0;
                            break;
                    }
                }

                pixels[i] = (int) v;
            }
        }
    }

}
