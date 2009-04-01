package com.g3d.util;

import com.g3d.texture.Image;
import com.g3d.texture.Image.Format;
import com.g3d.texture.Texture;
import com.g3d.texture.Texture2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

public class TextureLoader {

    public static Texture loadTexture(URL url) throws IOException{
        BufferedImage img = ImageIO.read(url);
        ByteBuffer data = BufferUtils.createByteBuffer(img.getWidth()*img.getHeight()*3);
        data.rewind();
        for (int y = 0; y < img.getHeight(); y++){
            for (int x = 0; x < img.getWidth(); x++){
                int rgb = img.getRGB(x,y);
                byte r = (byte) ((rgb & 0x00FF0000) >> 16);
                byte g = (byte) ((rgb & 0x0000FF00) >> 8);
                byte b = (byte) ((rgb & 0x000000FF));
//                if ((r & 0xFF) != 255){
//                    System.out.println(g & 0xFF);
//                }
                data.put(r).put(g).put(b);
            }
        }
        data.flip();
        Image image = new Image(Format.RGB8, img.getWidth(), img.getHeight(), data);
        Texture tex = new Texture2D();
        tex.setImage(image);
        return tex;
    }

}
