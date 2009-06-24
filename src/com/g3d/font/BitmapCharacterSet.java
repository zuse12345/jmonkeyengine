package com.g3d.font;

import java.util.Hashtable;
import java.util.Map;

public class BitmapCharacterSet {

    private int lineHeight;
    private int base;
    private int renderedSize;
    private int width;
    private int height;
    private Map<Integer, BitmapCharacter> characters;

    public BitmapCharacterSet() {
        characters = new Hashtable<Integer, BitmapCharacter>();
    }

    public BitmapCharacter getCharacter(int index){
        return characters.get(index);
    }

    public void addCharacter(int index, BitmapCharacter ch){
        characters.put(index, ch);
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public int getRenderedSize() {
        return renderedSize;
    }

    public void setRenderedSize(int renderedSize) {
        this.renderedSize = renderedSize;
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
}