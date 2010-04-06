package com.jme3.niftygui;

import com.jme3.asset.TextureKey;
import com.jme3.texture.Image;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture2D;
import de.lessvoid.nifty.spi.render.RenderImage;
import de.lessvoid.nifty.tools.Color;

public class RenderImageJme implements RenderImage {

    private NiftyJmeDisplay display;
    private Texture2D texture;
    private Image image;

    public RenderImageJme(String filename, boolean linear, NiftyJmeDisplay display){
        this.display = display;

        TextureKey key = new TextureKey(filename, true);

        key.setAnisotropy(0);
        key.setAsCube(false);
        key.setGenerateMips(false);
        
        texture = (Texture2D) display.getAssetManager().loadTexture(key);
        
//        if (linear){
            texture.setMagFilter(MagFilter.Bilinear);
            texture.setMinFilter(MinFilter.BilinearNoMipMaps);
//        }else{
//            texture.setMagFilter(MagFilter.Nearest);
//            texture.setMinFilter(MinFilter.NearestNoMipMaps);
//        }

        this.image = texture.getImage();
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public void render(int x, int y, int width, int height,
                       Color color, float imageScale){
        display.getRenderDevice().renderTextureQuad(x, y, width, height, color, imageScale, texture);
    }

    public void render(int x, int y, int w, int h,
                       int srcX, int srcY, int srcW, int srcH,
                       Color color, float scale,
                       int centerX, int centerY){
        display.getRenderDevice().renderTextureQuad(x, y, w, h, srcX, srcY, srcW, srcH, color, scale, centerX, centerY, texture);
    }
}
