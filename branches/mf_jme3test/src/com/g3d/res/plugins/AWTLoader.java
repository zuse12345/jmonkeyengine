package com.g3d.res.plugins;

import com.g3d.res.*;
import com.g3d.texture.Image;
import com.g3d.texture.Image.Format;
import com.g3d.util.BufferUtils;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

public class AWTLoader implements ContentLoader {

    private Image loadAWT(ContentManager owner, InputStream in) throws IOException{
        BufferedImage img = ImageIO.read(in);
        if (img == null)
            return null;

        int width = img.getWidth();
        int height = img.getHeight();
        String flipProp = owner.getProperty("FlipImages");
        boolean flip = flipProp == null || flipProp.equals("true");

        if (img.getTransparency() == Transparency.OPAQUE){
            ByteBuffer data = BufferUtils.createByteBuffer(img.getWidth()*img.getHeight()*3);
            // no alpha
            for (int y = 0; y < height; y++){
                for (int x = 0; x < width; x++){
                    int ny = y;
                    if (flip){
                        ny = height - y - 1;
                    }
                    
                    int rgb = img.getRGB(x,ny);
                    byte r = (byte) ((rgb & 0x00FF0000) >> 16);
                    byte g = (byte) ((rgb & 0x0000FF00) >> 8);
                    byte b = (byte) ((rgb & 0x000000FF));
                    data.put(r).put(g).put(b);
                }
            }
            data.flip();
            return new Image(Format.RGB8, width, height, data);
        }else{
            ByteBuffer data = BufferUtils.createByteBuffer(img.getWidth()*img.getHeight()*4);
            // no alpha
            for (int y = 0; y < height; y++){
                for (int x = 0; x < width; x++){
                    int ny = y;
                    if (flip){
                        ny = height - y - 1;
                    }

                    int rgb = img.getRGB(x,ny);
                    byte a = (byte) ((rgb & 0xFF000000) >> 24);
                    byte r = (byte) ((rgb & 0x00FF0000) >> 16);
                    byte g = (byte) ((rgb & 0x0000FF00) >> 8);
                    byte b = (byte) ((rgb & 0x000000FF));
                    data.put(r).put(g).put(b).put(a);
                }
            }
            data.flip();
            return new Image(Format.RGBA8, width, height, data);
        }
    }

    public Object load(ContentManager owner, InputStream in, String extension, ContentKey key) throws IOException {
        if (ImageIO.getImageWritersBySuffix(extension) != null){
            return loadAWT(owner, in);
        }
        return null;
    }
}
