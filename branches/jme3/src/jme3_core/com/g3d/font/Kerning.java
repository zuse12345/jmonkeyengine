package com.g3d.font;

import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import com.g3d.export.Savable;
import java.io.IOException;


/// <summary>Represents kerning information for a character.</summary>
public class Kerning implements Savable {

    private int second;
    private int amount;

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void write(G3DExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(second, "second", 0);
        oc.write(amount, "amount", 0);
    }

    public void read(G3DImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        second = ic.readInt("second", 0);
        amount = ic.readInt("amount", 0);
    }
}