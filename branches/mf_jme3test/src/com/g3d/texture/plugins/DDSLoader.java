/*
 * Copyright (c) 2003-2009 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.g3d.texture.plugins;

import com.g3d.asset.*;
import com.g3d.util.*;
import com.g3d.math.FastMath;
import com.g3d.asset.AssetLoader;
import com.g3d.texture.Image;
import com.g3d.texture.Image.Format;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.logging.Logger;

/**
 * 
 * <code>DDSLoader</code> is an image loader that reads in a DirectX DDS file.
 * Supports DXT1, DXT3, DXT5, RGB, RGBA, Grayscale, Alpha pixel formats.
 * 2D images, mipmapped 2D images, and cubemaps.
 * 
 * @author Gareth Jenkins-Jones
 * @author Kirill Vainer
 * @version $Id: DDSLoader.java,v 2.0 2008/8/15
 */
public class DDSLoader implements AssetLoader {

    private static final Logger logger = Logger.getLogger(DDSLoader.class.getName());
    private static final boolean forceRGBA = false;

    private static final int DDSD_MANDATORY = 0x1007;
    private static final int DDSD_MIPMAPCOUNT = 0x20000;
    private static final int DDSD_LINEARSIZE = 0x80000;
    private static final int DDSD_DEPTH = 0x800000;

    private static final int DDPF_ALPHAPIXELS = 0x1;
    private static final int DDPF_FOURCC = 0x4;
    private static final int DDPF_RGB = 0x40;
    // used by compressonator to mark grayscale images, red channel mask is used for data and bitcount is 8
    private static final int DDPF_GRAYSCALE = 0x20000;
    // used by compressonator to mark alpha images, alpha channel mask is used for data and bitcount is 8
    private static final int DDPF_ALPHA = 0x2;

    private static final int DDSCAPS_COMPLEX = 0x8;
    private static final int DDSCAPS_TEXTURE = 0x1000;
    private static final int DDSCAPS_MIPMAP = 0x400000;

    private static final int DDSCAPS2_CUBEMAP = 0x200;
    private static final int DDSCAPS2_VOLUME = 0x200000;

    private static final int PF_DXT1 = 0x31545844;
    private static final int PF_DXT3 = 0x33545844;
    private static final int PF_DXT5 = 0x35545844;

    private static final double LOG2 = Math.log(2);
    private int width;
    private int height;
    private int depth;
    private int flags;
    private int pitchOrSize;
    private int mipMapCount;
    private int caps1;
    private int caps2;
    private boolean compressed;
    private boolean grayscaleOrAlpha;
    private Format pixelFormat;
    private int bpp;
    private int[] sizes;
    private int redMask,  greenMask,  blueMask,  alphaMask;
    private DataInput in;

    public DDSLoader() {
    }

    public Object load(AssetInfo info) throws IOException{
        InputStream stream = info.openStream();
        in = new LittleEndien(stream);
        loadHeader();
        ArrayList<ByteBuffer> data = readData( ((TextureKey)info.getKey()).isFlipY() );
        stream.close();

        return new Image(pixelFormat, width, height, 0, data, sizes);
    }

    /**
     * Reads the header (first 128 bytes) of a DDS File
     */
    public void loadHeader() throws IOException {
        if (in.readInt() != 0x20534444 || in.readInt() != 124) {
            throw new IOException("Not a DDS file");
        }

        flags = in.readInt();

        if (!is(flags, DDSD_MANDATORY)) {
            throw new IOException("Mandatory flags missing");
        }
        
        height = in.readInt();
        width = in.readInt();
        pitchOrSize = in.readInt();
        depth = in.readInt();
        mipMapCount = in.readInt();
        in.skipBytes(44);
        readPixelFormat();
        caps1 = in.readInt();
        caps2 = in.readInt();
        in.skipBytes(12);

        if (!is(caps1, DDSCAPS_TEXTURE)) {
            throw new IOException("File is not a texture");
        }

        if (depth <= 0)
            depth = 1;

        int expectedMipmaps = 1 + (int) Math.ceil(Math.log(Math.max(height, width)) / LOG2);

        if (is(caps1, DDSCAPS_MIPMAP)) {
            if (!is(flags, DDSD_MIPMAPCOUNT)) {
                mipMapCount = expectedMipmaps;
            } else if (mipMapCount != expectedMipmaps) {
                // changed to warning- images often do not have the required amount,
                // or specify that they have mipmaps but include only the top level..
                logger.warning("Got " + mipMapCount + "mipmaps, expected" + expectedMipmaps);
            }
        } else {
            mipMapCount = 1;
        }

        loadSizes();
    }

    /**
     * Reads the PixelFormat structure in a DDS file
     */
    private void readPixelFormat() throws IOException {
        int pfSize = in.readInt();
        if (pfSize != 32) {
            throw new IOException("Pixel format size is " + pfSize + ", not 32");
        }

        int pfFlags = in.readInt();
        if (is(pfFlags, DDPF_FOURCC)) {
            compressed = true;
            int fourcc = in.readInt();
            in.skipBytes(20);

            switch (fourcc) {
                case PF_DXT1:
                    bpp = 4;
                    if (is(pfFlags, DDPF_ALPHAPIXELS)) {
                        pixelFormat = Image.Format.DXT1A;
                    } else {
                        pixelFormat = Image.Format.DXT1;
                    }
                    break;
                case PF_DXT3:
                    bpp = 8;
                    pixelFormat = Image.Format.DXT3;
                    break;
                case PF_DXT5:
                    bpp = 8;
                    pixelFormat = Image.Format.DXT5;
                    break;
                default:
                    throw new IOException("Unknown fourcc: " + string(fourcc));
            }

            int size = ((width + 3) / 4) * ((height + 3) / 4) * bpp * 2;

            if (is(flags, DDSD_LINEARSIZE)) {
                if (pitchOrSize == 0) {
                    logger.warning("Must use linear size with fourcc");
                    pitchOrSize = size;
                } else if (pitchOrSize != size) {
                    logger.warning("Expected size = " + size + ", real = " + pitchOrSize);
                }
            } else {
                pitchOrSize = size;
            }
        } else {
            compressed = false;

            // skip fourCC
            in.readInt();

            bpp = in.readInt();
            redMask = in.readInt();
            greenMask = in.readInt();
            blueMask = in.readInt();
            alphaMask = in.readInt();

            if (is(pfFlags, DDPF_RGB)) {
                if (is(pfFlags, DDPF_ALPHAPIXELS)) {
                    pixelFormat = Format.RGBA8;
                } else {
                    pixelFormat = Format.RGB8;
                }
            } else if (is(pfFlags, DDPF_GRAYSCALE) && is(pfFlags, DDPF_ALPHAPIXELS)){
                switch (bpp) {
                    case 16:
                        pixelFormat = Format.Luminance8Alpha8;
                        break;
                    case 32:
                        pixelFormat = Format.Luminance16Alpha16;
                        break;
                    default:
                        throw new IOException("Unsupported GrayscaleAlpha BPP: " + bpp);
                }
                grayscaleOrAlpha = true;
            } else if (is(pfFlags, DDPF_GRAYSCALE)) {
                switch (bpp) {
                    case 8:
                        pixelFormat = Format.Luminance8;
                        break;
                    case 16:
                        pixelFormat = Format.Luminance16;
                        break;
                    default:
                        throw new IOException("Unsupported Grayscale BPP: " + bpp);
                }
                grayscaleOrAlpha = true;
            } else if (is(pfFlags, DDPF_ALPHA)) {
                switch (bpp) {
                    case 8:
                        pixelFormat = Format.Alpha8;
                        break;
                    case 16:
                        pixelFormat = Format.Alpha16;
                        break;
                    default:
                        throw new IOException("Unsupported Alpha BPP: " + bpp);
                }
                grayscaleOrAlpha = true;
            } else {
                throw new IOException("Unknown PixelFormat in DDS file");
            }

            int size = (bpp / 8 * width);

            if (is(flags, DDSD_LINEARSIZE)) {
                if (pitchOrSize == 0) {
                    logger.warning("Linear size said to contain valid value but does not");
                    pitchOrSize = size;
                } else if (pitchOrSize != size) {
                    logger.warning("Expected size = " + size + ", real = " + pitchOrSize);
                }
            } else {
                pitchOrSize = size;
            }
        }
    }

    /**
     * Computes the sizes of each mipmap level in bytes, and stores it in sizes_[].
     */
    private void loadSizes() {
        int mipWidth = width;
        int mipHeight = height;

        sizes = new int[mipMapCount];
        for (int i = 0; i < mipMapCount; i++) {
            int size;
            if (compressed) {
                size = ((mipWidth + 3) / 4) * ((mipHeight + 3) / 4) * bpp * 2;
            } else {
                size = mipWidth * mipHeight * bpp / 8;
            }

            sizes[i] = ((size + 3) / 4) * 4;

            mipWidth = Math.max(mipWidth / 2, 1);
            mipHeight = Math.max(mipHeight / 2, 1);
        }
    }

    /**
     * Flips the given image data on the Y axis.
     * @param data Data array containing image data (without mipmaps)
     * @param scanlineSize Size of a single scanline = width * bytesPerPixel
     * @param height Height of the image in pixels
     * @return The new data flipped by the Y axis
     */
    public byte[] flipData(byte[] data, int scanlineSize, int height) {
        byte[] newData = new byte[data.length];

        for (int y = 0; y < height; y++) {
            System.arraycopy(data, y * scanlineSize,
                    newData, (height - y - 1) * scanlineSize,
                    scanlineSize);
        }

        return newData;
    }

    /**
     * Reads a grayscale image with mipmaps from the InputStream
     * @param flip Flip the loaded image by Y axis
     * @param totalSize Total size of the image in bytes including the mipmaps
     * @return A ByteBuffer containing the grayscale image data with mips.
     * @throws java.io.IOException If an error occured while reading from InputStream
     */
    public ByteBuffer readGrayscale2D(boolean flip, int totalSize) throws IOException {
        ByteBuffer buffer = BufferUtils.createByteBuffer(totalSize);

        if (bpp == 8) {
            logger.finest("Source image format: R8");
        }

        assert bpp == pixelFormat.getBitsPerPixel();

        int mipWidth = width;
        int mipHeight = height;

        for (int mip = 0; mip < mipMapCount; mip++) {
            byte[] data = new byte[sizes[mip]];
            in.readFully(data);
            if (flip) {
                data = flipData(data, mipWidth * bpp / 8, mipHeight);
            }
            buffer.put(data);

            mipWidth = Math.max(mipWidth / 2, 1);
            mipHeight = Math.max(mipHeight / 2, 1);
        }

        return buffer;
    }

    /**
     * Reads an uncompressed RGB or RGBA image.
     *
     * @param flip Flip the image on the Y axis
     * @param totalSize Size of the image in bytes including mipmaps
     * @return ByteBuffer containing image data with mipmaps in the format specified by pixelFormat_
     * @throws java.io.IOException If an error occured while reading from InputStream
     */
    public ByteBuffer readRGB2D(boolean flip, int totalSize) throws IOException {
        int redCount = count(redMask),
            blueCount = count(blueMask),
            greenCount = count(greenMask),
            alphaCount = count(alphaMask);

        if (redMask == 0x00FF0000 && greenMask == 0x0000FF00 && blueMask == 0x000000FF) {
            if (alphaMask == 0xFF000000 && bpp == 32) {
                logger.finest("Data source format: BGRA8");
            } else if (bpp == 24) {
                logger.finest("Data source format: BGR8");
            }
        }

        int sourcebytesPP = bpp / 8;
        int targetBytesPP = pixelFormat.getBitsPerPixel() / 8;

        ByteBuffer dataBuffer = BufferUtils.createByteBuffer(totalSize);

        int mipWidth = width;
        int mipHeight = height;

        int offset = 0;
        for (int mip = 0; mip < mipMapCount; mip++) {
            for (int y = 0; y < mipHeight; y++) {
                for (int x = 0; x < mipWidth; x++) {
                    byte[] b = new byte[sourcebytesPP];
                    in.readFully(b);

                    int i = byte2int(b);

                    byte red = (byte) (((i & redMask) >> redCount));
                    byte green = (byte) (((i & greenMask) >> greenCount));
                    byte blue = (byte) (((i & blueMask) >> blueCount));
                    byte alpha = (byte) (((i & alphaMask) >> alphaCount));

                    if (flip) {
                        dataBuffer.position(offset + ((mipHeight - y - 1) * mipWidth + x) * targetBytesPP);
                    }
                    //else
                    //    dataBuffer.position(offset + (y * width + x) * targetBytesPP);

                    if (alphaMask == 0) {
                        dataBuffer.put(red).put(green).put(blue);
                    } else {
                        dataBuffer.put(red).put(green).put(blue).put(alpha);
                    }
                }
            }

            offset += mipWidth * mipHeight * targetBytesPP;

            mipWidth = Math.max(mipWidth / 2, 1);
            mipHeight = Math.max(mipHeight / 2, 1);
        }

        return dataBuffer;
    }

    private static long alphaBitsToLong(byte[] block){
        return  (block[7] & 0xFF)
             | ((block[6] & 0xFF) << 8)
             | ((block[5] & 0xFF) << 16)
             | ((block[4] & 0xFF) << 24)
             | ((block[3] & 0xFF) << 32)
             | ((block[2] & 0xFF) << 48);
    }

    private static void longToAlphaBits(long block, byte[] store){
        store[2] = (byte) (block & 0xFF);
        store[3] = (byte) ((block >> 8) & 0xFF);
        store[4] = (byte) ((block >> 16) & 0xFF);
        store[5] = (byte) ((block >> 24) & 0xFF);
        store[6] = (byte) ((block >> 32) & 0xFF);
        store[7] = (byte) ((block >> 48) & 0xFF);
    }

    private static final BitSet fromByteArray(byte[] bytes) {
        BitSet bits = new BitSet();
        for (int i=0; i<bytes.length*8; i++) {
            if ((bytes[bytes.length-i/8-1]&(1<<(i%8))) > 0) {
                bits.set(i);
            }
        }
        return bits;
    }

    public static byte[] toByteArray(BitSet bits) {
        byte[] bytes = new byte[bits.length()/8+1];
        for (int i=0; i<bits.length(); i++) {
            if (bits.get(i)) {
                bytes[bytes.length-i/8-1] |= 1<<(i%8);
            }
        }
        return bytes;
    }

    private static final void copyBits(BigInteger bi, int start1, int len1, int start2){
        int i2 = start2;
        for (int i = start1; i < start1 + len1; i++){
            boolean bit = bi.testBit(i);
            boolean bit2 = bi.testBit(i2);
            if (bit2 == bit)
                continue;
            else{
                bi.flipBit(i2);
            }
            
            i2 ++;
        }
    }

    private static final int idx(int x, int y){
        return 16 + y * 12 + x;
    }

    private void flipDXT5AlphaBlock(byte[] block, int h){
        byte tmp;
        switch (h){
            case 1:
                return;
            case 2:
                // top left to bottom left
                tmp = block[0];
                block[0] = block[2];
                block[2] = tmp;

                // top right to bottom right
                tmp = block[1];
                block[1] = block[3];
                block[3] = tmp;
                return;
            default:
                BigInteger bi = new BigInteger(block);

                // first two bytes are alpha, keep the same
                copyBits(bi, idx(0,0), 12, idx(0,3));
                copyBits(bi, idx(0,1), 12, idx(0,2));
                copyBits(bi, idx(0,2), 12, idx(0,1));
                copyBits(bi, idx(0,3), 12, idx(0,0));

                byte[] out = bi.toByteArray();
                System.arraycopy(out, 0, block, 0, 8);
                
                return;
        }
    }

    /**
     * Flips a DXT color block or a DXT3 alpha block
     * @param block
     * @param h
     */
    public void flipDXTBlock(byte[] block, int h){
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

    public ByteBuffer flipDXT1(ByteBuffer img, int w, int h){
        int blocksX = (int) FastMath.ceil((float)w / 4f);
        int blocksY = (int) FastMath.ceil((float)h / 4f);

        int type = 0;
        if (pixelFormat == Format.DXT1 || pixelFormat == Format.DXT1A){
            type = 1;
        }else if (pixelFormat == Format.DXT3){
            type = 2;
        }else if (pixelFormat == Format.DXT5){
            type = 3;
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
                flipDXTBlock(colorBlock, h);

                // write block (no need to flip block indexes, only pixels
                // inside block
                retImg.put(colorBlock);

                if (alphaBlock != null){
                    img.get(alphaBlock);
                    switch (type){
                        case 2: flipDXTBlock(alphaBlock, h); break;
                        case 3: flipDXT5AlphaBlock(alphaBlock, h); break;
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
                            case 2: flipDXTBlock(alphaBlock, h); break;
                            case 3: flipDXT5AlphaBlock(alphaBlock, h); break;
                        }
                        retImg.put(alphaBlock);
                    }

                    img.get(colorBlock);
                    flipDXTBlock(colorBlock, h);
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

    /**
     * Reads a DXT compressed image from the InputStream
     *
     * @param totalSize Total size of the image in bytes, including mipmaps
     * @return ByteBuffer containing compressed DXT image in the format specified by pixelFormat_
     * @throws java.io.IOException If an error occured while reading from InputStream
     */
    public ByteBuffer readDXT2D(boolean flip, int totalSize) throws IOException {
        logger.finest("Source image format: DXT");

        ByteBuffer buffer = BufferUtils.createByteBuffer(totalSize);

        int mipWidth = width;
        int mipHeight = height;

        int offset = 0;
        for (int mip = 0; mip < mipMapCount; mip++) {
            if (flip){
                byte[] data = new byte[sizes[mip]];
                in.readFully(data);
                ByteBuffer wrapped = ByteBuffer.wrap(data);
                wrapped.rewind();
                ByteBuffer flipped = flipDXT1(wrapped, mipWidth, mipHeight);
                buffer.put(flipped);
            }else{
                byte[] data = new byte[sizes[mip]];
                in.readFully(data);
                buffer.put(data);
            }

            offset += sizes[mip];

            mipWidth = Math.max(mipWidth / 2, 1);
            mipHeight = Math.max(mipHeight / 2, 1);
        }
        buffer.rewind();

        return buffer;
    }

    /**
     * Reads the image data from the InputStream in the required format.
     * If the file contains a cubemap image, it is loaded as 6 ByteBuffers
     * (potentially containing mipmaps if they were specified), otherwise
     * a single ByteBuffer is returned for a 2D image.
     *
     * @param flip Flip the image data or not.
     *        For cubemaps, each of the cubemap faces is flipped individually.
     *        If the image is DXT compressed, no flipping is done.
     * @return An ArrayList containing a single ByteBuffer for a 2D image, or 6 ByteBuffers for a cubemap.
     *         The cubemap ByteBuffer order is PositiveX, NegativeX, PositiveY, NegativeY, PositiveZ, NegativeZ.
     *
     * @throws java.io.IOException If an error occured while reading from the stream.
     */
    public ArrayList<ByteBuffer> readData(boolean flip) throws IOException {
        int totalSize = 0;

        for (int i = 0; i < sizes.length; i++) {
            totalSize += sizes[i];
        }

        ArrayList<ByteBuffer> allMaps = new ArrayList<ByteBuffer>();
        if (is(caps2, DDSCAPS2_CUBEMAP)) {
            for (int i = 0; i < 6; i++) {
                if (compressed) {
                    allMaps.add(readDXT2D(flip,totalSize));
                } else if (grayscaleOrAlpha) {
                    allMaps.add(readGrayscale2D(flip, totalSize));
                } else {
                    allMaps.add(readRGB2D(flip, totalSize));
                }
            }
        } else if (depth > 1){
            for (int i = 0; i < depth; i++){
                if (compressed) {
                    allMaps.add(readDXT2D(flip,totalSize));
                } else if (grayscaleOrAlpha) {
                    allMaps.add(readGrayscale2D(flip, totalSize));
                } else {
                    allMaps.add(readRGB2D(flip, totalSize));
                }
            }
        } else {
            if (compressed) {
                allMaps.add(readDXT2D(flip,totalSize));
            } else if (grayscaleOrAlpha) {
                allMaps.add(readGrayscale2D(flip, totalSize));
            } else {
                allMaps.add(readRGB2D(flip, totalSize));
            }
        }

        return allMaps;
    }

    /**
     * Checks if flags contains the specified mask
     */
    private static final boolean is(int flags, int mask) {
        return (flags & mask) == mask;
    }

    /**
     * Counts the amount of bits needed to shift till bitmask n is at zero
     * @param n Bitmask to test
     */
    private static int count(int n) {
        if (n == 0) {
            return 0;
        }

        int i = 0;
        while ((n & 0x1) == 0) {
            n = n >> 1;
            i++;
            if (i > 32) {
                throw new RuntimeException(Integer.toHexString(n));
            }
        }

        return i;
    }

    /**
     * Converts a 1 to 4 sized byte array to an integer
     */
    private static int byte2int(byte[] b) {
        if (b.length == 1) {
            return b[0] & 0xFF;
        } else if (b.length == 2) {
            return (b[0] & 0xFF) | ((b[1] & 0xFF) << 8);
        } else if (b.length == 3) {
            return (b[0] & 0xFF) | ((b[1] & 0xFF) << 8) | ((b[2] & 0xFF) << 16);
        } else if (b.length == 4) {
            return (b[0] & 0xFF) | ((b[1] & 0xFF) << 8) | ((b[2] & 0xFF) << 16) | ((b[3] & 0xFF) << 24);
        } else {
            return 0;
        }
    }

    /**
     * Converts a int representing a FourCC into a String
     */
    private static final String string(int value) {
        StringBuffer buf = new StringBuffer();

        buf.append((char) (value & 0xFF));
        buf.append((char) ((value & 0xFF00) >> 8));
        buf.append((char) ((value & 0xFF0000) >> 16));
        buf.append((char) ((value & 0xFF00000) >> 24));

        return buf.toString();
    }
}
