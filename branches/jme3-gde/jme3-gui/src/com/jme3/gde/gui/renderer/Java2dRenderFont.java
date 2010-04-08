/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.gde.gui.renderer;

import de.lessvoid.nifty.spi.render.RenderFont;
import de.lessvoid.nifty.tools.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author normenhansen
 */
public class Java2dRenderFont implements RenderFont{
    private String name;
    private Font font;
    private BufferedImage image;

    //all sizes TODO!

    public Java2dRenderFont(String name, BufferedImage image) {
        this.image=image;
        this.name=name;
        font=new Font(name, 0, 10);
    }


    public int getWidth(String text) {
        return font.getSize()*text.length();
    }

    public int getHeight() {
        return font.getSize();
    }

    public void render(String text, int x, int y, Color fontColor, float size) {
        font=new Font(name, 0, Math.round(size*20));
        Graphics2D g2d=(Graphics2D)image.getGraphics();
        g2d.setFont(font);
        g2d.setColor(Java2dRenderDevice.color(fontColor));
        g2d.drawString(text, x, y);
    }

    public Integer getCharacterAdvance(char currentCharacter, char nextCharacter, float size) {
        return font.getSize();
    }

}
