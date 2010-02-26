package com.jme3.font;

import com.jme3.export.G3DExporter;
import com.jme3.export.G3DImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single bitmap character.
 */
public class BitmapCharacter implements Savable {

    private int x;
    private int y;
    private int width;
    private int height;
    private int xOffset;
    private int yOffset;
    private int xAdvance;
    private ArrayList<Kerning> kerningList = new ArrayList<Kerning>();

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

    public void setKerningList(ArrayList<Kerning> kerningList) {
        this.kerningList = kerningList;
    }

    public void write(G3DExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(x, "x", 0);
        oc.write(y, "y", 0);
        oc.write(width, "width", 0);
        oc.write(height, "height", 0);
        oc.write(xOffset, "xOffset", 0);
        oc.write(yOffset, "yOffset", 0);
        oc.write(xAdvance, "xAdvance", 0);
        oc.writeSavableArrayList(kerningList, "kerningList", null);
    }

    public void read(G3DImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        x = ic.readInt("x", 0);
        y = ic.readInt("y", 0);
        width = ic.readInt("width", 0);
        height = ic.readInt("height", 0);
        xOffset = ic.readInt("xOffset", 0);
        yOffset = ic.readInt("yOffset", 0);
        xAdvance = ic.readInt("xAdvance", 0);
        kerningList = (ArrayList<Kerning>) ic.readSavableArrayList("kerningList", null);
    }
}