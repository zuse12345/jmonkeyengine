/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.gde.gui.renderer;

import de.lessvoid.nifty.spi.render.RenderImage;
import de.lessvoid.nifty.tools.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 *
 * @author normenhansen
 */
public class Java2dRenderImage implements RenderImage{
    private BufferedImage image;

    public Java2dRenderImage(String name) {
        URL url=null;
        //TODO: what is standard, path or classpath?
        File file=new File(name);
        if(file.exists()){
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, "found image in folder");
            try {
                url = file.toURI().toURL();
            } catch (MalformedURLException ex) {
                Logger.getLogger(Java2dRenderImage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, "found image in classpath");
            url=Java2dRenderImage.class.getClassLoader().getResource(name);
        }
        if(url==null){
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "could not find image");
            image =
              new BufferedImage(
                  1024,
                  768,
                  BufferedImage.TYPE_INT_ARGB);
        }
        else{
            ImageIcon icon = new ImageIcon(url);
            Image image2 = icon.getImage();
            // Create empty BufferedImage, sized to Image
            image =
              new BufferedImage(
                  image2.getWidth(null),
                  image2.getHeight(null),
                  BufferedImage.TYPE_INT_ARGB);

            // Draw Image into BufferedImage
            Graphics g = image.getGraphics();
            g.drawImage(image2, 0, 0, null);
        }
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public BufferedImage getImage(){
        return image;
    }

    /**deprecated**/
    public void render(int i, int i1, int i2, int i3, Color color, float f) {
    }

    /**deprecated**/
    public void render(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, Color color, float f, int i8, int i9) {
    }

}
