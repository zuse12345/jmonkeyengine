package com.jme3.niftygui;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.texture.Texture;
import de.lessvoid.nifty.spi.render.RenderFont;
import de.lessvoid.nifty.tools.Color;

public class RenderFontJme implements RenderFont {

    private NiftyJmeDisplay display;
    private BitmapFont font;
    private BitmapText text;
    private Texture texture;
    private float actualSize;

    /**
     * Initialize the font.
     * @param name font filename
     */
    public RenderFontJme(String name, NiftyJmeDisplay display) {
        this.display = display;
        font = display.getAssetManager().loadFont(name);
        texture = font.getPage(0).getTextureParam("m_Texture").getTextureValue();
        text = new BitmapText(font);
        actualSize = font.getPreferredSize();
        text.setSize(actualSize);
    }

    /**
     * render the text.
     * @param text text
     * @param x x
     * @param y y
     * @param color color
     * @param fontSize size
     */
    public void render(final String str, final int x, final int y, final Color color, final float fontSize) {
        display.getRenderDevice().renderText(str, x, y, color, fontSize, text, texture);
    }

    /**
     * get font height.
     * @return height
     */
    public int getHeight() {
        return (int) text.getLineHeight();
    }

    /**
     * get font width of the given string.
     * @param text text
     * @return width of the given text for the current font
     */
    public int getWidth(final String str) {
        if (str.length() == 0)
            return 0;
        
        int result = (int) font.getLineWidth(str);
//        text.setText(str);
//        text.updateLogicalState(0);
//        int result = (int) text.getLineWidth();

        return result;
    }

    /**
     * Return the width of the given character including kerning information.
     * @param currentCharacter current character
     * @param nextCharacter next character
     * @param size font size
     * @return width of the character or null when no information for the character is available
     */
    public Integer getCharacterAdvance(final char currentCharacter, final char nextCharacter, final float size) {
        return null;
        //        CharacterInfo currentCharacterInfo = font.getChar(currentCharacter);
//        if (currentCharacterInfo == null) {
//            return null;
//        } else {
//            return new Integer(
//                    (int) (currentCharacterInfo.getXadvance() * size + getKerning(currentCharacterInfo, nextCharacter)));
//        }
    }
}