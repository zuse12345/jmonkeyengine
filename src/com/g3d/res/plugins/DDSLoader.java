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
package com.g3d.res.plugins;

import com.g3d.util.LittleEndien;
import com.g3d.res.*;
import com.g3d.util.*;
import com.g3d.math.FastMath;
import com.g3d.res.ContentLoader;
import com.g3d.texture.Image;
import com.g3d.texture.Image.Format;
import com.g3d.texture.Texture;
import com.g3d.texture.Texture2D;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import java.util.ArrayList;
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
public class DDSLoader implements ContentLoader {

    private static final Logger logger = Logger.getLogger(DDSLoader.class.getName());

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
    private int depth; // currently unused
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

    public void setOwner(ContentManager owner){
    }

    public Object load(InputStream in, String extension) throws IOException{
        return loadImage(in);
    }

    public Image loadImage(InputStream fis) throws IOException {
        return loadImage(fis, false);
    }

    public Image loadImage(InputStream fis, boolean flip) throws IOException {
        in = new LittleEndien(fis);
        loadHeader();
        ArrayList<ByteBuffer> data = readData(flip);

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
        if (is(flags, DDSD_DEPTH)) {
            throw new IOException("Depth not supported");
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

        if (is(caps2, DDSCAPS2_VOLUME)) {
            throw new IOException("Volume textures not supported");
        } else {
            depth = 0;
        }

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
            } else if (is(pfFlags, DDPF_GRAYSCALE)) {
                switch (bpp) {
//                    case 4:
//                        pixelFormat_ = Format.Luminance4;
//                        break;
                    case 8:
                        pixelFormat = Format.Luminance8;
                        break;
//                    case 12:
//                        pixelFormat_ = Format.Luminance12;
//                        break;
                    case 16:
                        pixelFormat = Format.Luminance16;
                        break;
                    default:
                        throw new IOException("Unsupported Grayscale BPP: " + bpp);
                }
                grayscaleOrAlpha = true;
            } else if (is(pfFlags, DDPF_ALPHA)) {
                switch (bpp) {
//                    case 4:
//                        pixelFormat_ = Format.Alpha4;
//                        break;
                    case 8:
                        pixelFormat = Format.Alpha8;
                        break;
//                    case 12:
//                        pixelFormat_ = Format.Alpha12;
//                        break;
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

    public void flipDXT1Block(ByteBuffer block, ByteBuffer flipped, int h){
        assert block.remaining() == 8;
        assert flipped.remaining() == 8;

        int oldPos = block.position();
        int oldLimit = block.limit();

        if (h == 1){
            flipped.put(block);
        }if (h == 2){
            // write header (the two colors)
            block.limit(block.capacity());
            block.position(oldPos);
            block.limit(oldPos + 4);
            flipped.put(block);

            // grab 2nd row (1 byte)
            block.limit(block.capacity());
            block.position(oldPos + 4 + 1);
            block.limit(block.position() + 1);
            flipped.put(block);

            // grab 1st row
            block.limit(block.capacity());
            block.position(oldPos + 4);
            block.limit(block.position() + 1);
            flipped.put(block);
        }else if (h >= 4){
            // write header (the two colors)
            block.limit(block.capacity());
            block.position(oldPos);
            block.limit(oldPos + 4);
            flipped.put(block);

            // grab 4th row (1 byte)
            block.limit(block.capacity());
            block.position(oldPos + 4 + 3);
            block.limit(block.position() + 1);
            flipped.put(block);

            // grab 3rd row
            block.limit(block.capacity());
            block.position(oldPos + 4 + 2);
            block.limit(block.position() + 1);
            flipped.put(block);

            // grab 2nd row
            block.limit(block.capacity());
            block.position(oldPos + 4 + 1);
            block.limit(block.position() + 1);
            flipped.put(block);

            // grab 1st row
            block.limit(block.capacity());
            block.position(oldPos + 4);
            block.limit(block.position() + 1);
            flipped.put(block);
        }
    }

    public ByteBuffer flipDXT1(ByteBuffer img, int w, int h){
        int blocksX = (int) FastMath.ceil((float)w / 4f);
        int blocksY = (int) FastMath.ceil((float)h / 4f);

        ByteBuffer retImg = BufferUtils.createByteBuffer(blocksX * blocksY * 8);
        retImg.rewind();

//        img.position(blocksX * 8);
//        img.limit(img.position() + 8);

        if (h == 1){
            retImg.put(img);
            retImg.rewind();
            return retImg;
        }else if (h == 2){
            ByteBuffer temp = BufferUtils.createByteBuffer(8);
            temp.rewind();
            for (int x = 0; x < blocksX; x++){
                // prepeare for block reading
                int blockByteOffset = x * 8;
                img.position(blockByteOffset);
                img.limit(blockByteOffset + 8);

                // create block
                ByteBuffer block = img.slice();

                // flip block
                flipDXT1Block(block, temp, h);
                temp.rewind();

                // write block (no need to flip block indexes, only pixels
                // inside block
                retImg.put(temp);
                temp.rewind();
            }
            retImg.rewind();
            return retImg;
        }else if (h >= 4){
            ByteBuffer temp = BufferUtils.createByteBuffer(8);
            temp.rewind();
            for (int y = 0; y < blocksY; y++){
                for (int x = 0; x < blocksX; x++){
//                    int offset = 8 * (FastMath.ceil(w / 4f) * floor(y / 4f) + floor(<x>/4)).

                    // prepeare for block reading
                    int blockIdx = y * blocksX + x;
                    int blockByteOffset = blockIdx * 8; // 1 block is 8 bytes

                    img.position(blockByteOffset);
                    img.limit(blockByteOffset + 8);
                    ByteBuffer block = img.slice();


                    flipDXT1Block(block, temp, h);
                    temp.rewind();

                    // flip block location in image
                    blockIdx = blocksX + x;
                    blockByteOffset = blockIdx * 8;

                    retImg.position(blockByteOffset);
                    retImg.limit(blockByteOffset + 8);
                    retImg.put(temp);
//                    if (x % 2 == 0 && y % 2 == 0)
//                        retImg.put(block);
                    temp.rewind();
                }
            }
            retImg.rewind();
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
                // flip the stuff.. etc
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
