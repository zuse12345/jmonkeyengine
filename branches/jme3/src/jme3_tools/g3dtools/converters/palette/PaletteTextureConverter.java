package g3dtools.converters.palette;

import com.g3d.texture.Image;
import com.g3d.texture.Image.Format;
import com.g3d.util.BufferUtils;
import g3dtools.converters.ImageToAwt;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PaletteTextureConverter {

    public static BufferedImage decodePaletteTexture(Image image, int mipLevel){
        Format fmt = image.getFormat();
        boolean is16bit = fmt == Format.Pal4_RGB565
                       || fmt == Format.Pal8_RGB565;
        boolean isSmallPal = fmt == Format.Pal4_RGB565
                          || fmt == Format.Pal4_RGB8;

        int[] colorTable;
        if (isSmallPal)
            colorTable = new int[16];
        else
            colorTable = new int[256];

        ByteBuffer data = image.getData(0);
        data.order(ByteOrder.LITTLE_ENDIAN);
        data.clear();
        
        for (int i = 0; i < colorTable.length; i++){
            if (is16bit){
                colorTable[i] = PaletteUtil.RGB565_to_ARGB8(data.getShort());
            }else{
                colorTable[i] = data.getInt();
            }
        }

        int width = image.getWidth();
        int height = image.getHeight();

        int level = mipLevel;
        while (--level >= 0){
            width  /= 2;
            height /= 2;
        }

        int mipPos = 0;
        for (int i = 0; i < mipLevel; i++){
            mipPos += image.getMipMapSizes()[i];
        }

        int numIndices = width * height;
        if (isSmallPal)
            numIndices /= 2;

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        if (mipLevel > 0)
            data.position(mipPos);
        
        for (int i = 0; i < numIndices; i++){
            if (isSmallPal){
                int idx = data.get() & 0xff;
                int idx1 = (idx & 0xf0) >> 4;
                int idx2 = (idx & 0x0f);
                
                int y1     = (i*2)   / width;
                int x1     = (i*2)   % width;
                int y2     = (i*2+1) / width;
                int x2     = (i*2+1) % width;

                output.setRGB(x1, y1, colorTable[idx1]);
                output.setRGB(x2, y2, colorTable[idx2]);
            }else{
                int index = data.get() & 0xff;
                int y     = i / width;
                int x     = i % width;
                output.setRGB(x, y, colorTable[index]);
            }
        }

        return output;
    }

    private static byte[] getIndices(RenderedImage img, boolean is4bit){
        Raster raster = img.getData();
        byte[] indices = new byte[img.getWidth() * img.getHeight()];
        raster.getDataElements(0, 0, img.getWidth(), img.getHeight(), indices);
        
        if (is4bit){
            // 4 bit, one byte contains 2 indices
            byte[] shrunkIndices = new byte[indices.length / 2];
            for (int i = 0; i < shrunkIndices.length; i++){
                // extract lower 4 bits for each index
                int idx1 = indices[i*2]   & 0x0f;
                int idx2 = indices[i*2+1] & 0x0f;

                // merge them and store
                shrunkIndices[i] = (byte) ((idx1 << 4) | idx2);
            }
            return shrunkIndices;
        }else{
            // 8 bit, one byte contains one index
            return indices;
        }
    }
    
    private static void writeIndices(RenderedImage img, boolean is4bit, ByteBuffer out){
        byte[] indices = getIndices(img, is4bit);
        out.put(indices);
    }

    private static void writePalette(IndexColorModel model, boolean isSmallPal, boolean is16bit, ByteBuffer out){
        int[] rgbTable = new int[isSmallPal ? 16 : 256];
        model.getRGBs(rgbTable);

        if (is16bit){
            // convert the color table to RGB565 format
            for (int i = 0; i < rgbTable.length; i++){
                out.putShort(PaletteUtil.ARGB8_to_RGB565(rgbTable[i]));
            }
        }else{
            for (int i = 0; i < rgbTable.length; i++){
                out.putInt(rgbTable[i]);
            }
        }
    }

    public static Image encodePaletteTexture(Image input) {
        // format to convert into
        Format fmt = Format.Pal8_RGB565;

        // are colors 16 bit
        boolean is16bit = fmt == Format.Pal4_RGB565
                       || fmt == Format.Pal8_RGB565;

        // does palette consist of 16 entries or 256 (small or large)
        boolean isSmallPal = fmt == Format.Pal4_RGB565
                          || fmt == Format.Pal4_RGB8;

        // size of palette
        int tableSize = isSmallPal ? 16 : 256;

        // size of color entry in palette
        int colorSize = (is16bit ? 2 : 4);

        // size of palette in bytes
        int tableSizeBytes = tableSize * colorSize;

        // size of index in bits
        int indexSizeBits = isSmallPal ? 4 : 8;
        
        // determine mipmap sizes structure for output image
        int[] mipSizes;
        if (input.hasMipmaps()){
            mipSizes = new int[input.getMipMapSizes().length];
        }else{
            mipSizes = new int[1];
        }

        // convert jme images to AWT images, downscale to RGB565 if required
        // by output format
        int outputSize = 0;
        BufferedImage[] sourceImages = new BufferedImage[mipSizes.length];
        for (int i = 0; i < mipSizes.length; i++){
            sourceImages[i] = ImageToAwt.convert(input, is16bit, i);
            mipSizes[i] =  (sourceImages[i].getWidth()
                          * sourceImages[i].getHeight()
                          * indexSizeBits) / 8;
            outputSize += mipSizes[i];
        }
        // first mipmap level also contains palette
        mipSizes[0] += tableSizeBytes; 
        outputSize  += tableSizeBytes;

        // create palette for first image
        PaletteBuilderWrapper palBuilder = new PaletteBuilderWrapper();
        palBuilder.initialize(sourceImages[0], tableSize);
        palBuilder.buildPalette();

        // aquire palette of first image
        BufferedImage targetImage = (BufferedImage) palBuilder.getIndexedImage();
        IndexColorModel colorModel = (IndexColorModel) targetImage.getColorModel();

        // create output buffer
        ByteBuffer outputBuf = BufferUtils.createByteBuffer(outputSize);
        outputBuf.order(ByteOrder.LITTLE_ENDIAN);

        // write palette to buffer
        writePalette(colorModel, isSmallPal, is16bit, outputBuf);

        // write indices for first image
        writeIndices(targetImage, isSmallPal, outputBuf);

        // generate the rest of the mipmaps based on the palette of the
        // first image
        for (int i = 1; i < mipSizes.length; i++){
            palBuilder.setSourceImage(sourceImages[i]);
            targetImage = (BufferedImage) palBuilder.getIndexedImage();

            writeIndices(targetImage, isSmallPal, outputBuf);
        }

        Image output = new Image(fmt, input.getWidth(), input.getHeight(), outputBuf);
        if (mipSizes.length > 1)
            output.setMipMapSizes(mipSizes);

        return output;
    }

}
