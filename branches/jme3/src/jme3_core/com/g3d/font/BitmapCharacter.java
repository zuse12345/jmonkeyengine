package com.g3d.font;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single bitmap character.
 */
public class BitmapCharacter {

    private int x;
    private int y;
    private int width;
    private int height;
    private int xOffset;
    private int yOffset;
    private int xAdvance;
    private List<Kerning> kerningList = new ArrayList<Kerning>();

    // / <summary>Clones the BitmapCharacter</summary>
    // / <returns>Cloned BitmapCharacter</returns>
    public BitmapCharacter Clone() {
        BitmapCharacter result = new BitmapCharacter();
        result.x = x;
        result.y = y;
        result.width = width;
        result.height = height;
        result.xOffset = xOffset;
        result.yOffset = yOffset;
        result.xAdvance = xAdvance;
        result.kerningList.addAll(kerningList);
        return result;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getXOffset() {
        return xOffset;
    }

    public void setXOffset(int offset) {
        xOffset = offset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public void setYOffset(int offset) {
        yOffset = offset;
    }

    public int getXAdvance() {
        return xAdvance;
    }

    public void setXAdvance(int advance) {
        xAdvance = advance;
    }

    public List<Kerning> getKerningList() {
        return kerningList;
    }

    public void setKerningList(List<Kerning> kerningList) {
        this.kerningList = kerningList;
    }
}