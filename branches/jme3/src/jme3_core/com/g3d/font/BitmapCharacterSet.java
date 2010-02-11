package com.g3d.font;

import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import com.g3d.export.Savable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class BitmapCharacterSet implements Savable {

    private int lineHeight;
    private int base;
    private int renderedSize;
    private int width;
    private int height;
    private HashMap<Integer, BitmapCharacter> characters;

    public void write(G3DExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(lineHeight, "lineHeight", 0);
        oc.write(base, "base", 0);
        oc.write(renderedSize, "renderedSize", 0);
        oc.write(width, "width", 0);
        oc.write(height, "height", 0);

        Set<Entry<Integer, BitmapCharacter>> entries = characters.entrySet();
        short[] indexes = new short[entries.size()];
        BitmapCharacter[] chars = new BitmapCharacter[entries.size()];
        int i = 0;
        for (Entry<Integer, BitmapCharacter> chr : entries){
            indexes[i] = chr.getKey().shortValue();
            chars[i] = chr.getValue();
            i++;
        }

        oc.write(indexes, "indexes", null);
        oc.write(chars,   "chars",   null);
    }

    public void read(G3DImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        lineHeight = ic.readInt("lineHeight", 0);
        base = ic.readInt("base", 0);
        renderedSize = ic.readInt("renderedSize", 0);
        width = ic.readInt("width", 0);
        height = ic.readInt("height", 0);

        short[] indexes = ic.readShortArray("indexes", null);
        Savable[] chars = ic.readSavableArray("chars", null);

        for (int i = 0; i < indexes.length; i++){
            int index = indexes[i] & 0xFFFF;
            BitmapCharacter chr = (BitmapCharacter) chars[i];
            characters.put(index, chr);
        }
    }

    public BitmapCharacterSet() {
        characters = new HashMap<Integer, BitmapCharacter>();
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