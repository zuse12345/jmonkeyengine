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
//    private BufferedImage image;

    //all sizes TODO!

    public Java2dRenderFont(String name) {
//        this.image=image;
        this.name=name;
        font=new Font(name, 0, 10);
    }


    public int getWidth(String text) {
        return getFont().getSize()*text.length();
    }

    public int getHeight() {
        return getFont().getSize();
    }

    /**deprecated**/
    public void render(String text, int x, int y, Color fontColor, float size) {
    }

    public Integer getCharacterAdvance(char currentCharacter, char nextCharacter, float size) {
        return getFont().getSize();
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

}
