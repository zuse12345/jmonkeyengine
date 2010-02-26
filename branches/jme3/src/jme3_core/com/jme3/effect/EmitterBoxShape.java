package com.jme3.effect;

import com.jme3.export.G3DExporter;
import com.jme3.export.G3DImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import java.io.IOException;

public class EmitterBoxShape implements EmitterShape {

    private Vector3f min, len;

    public EmitterBoxShape(){
    }

    public EmitterBoxShape(Vector3f min, Vector3f max) {
        if (min == null || max == null)
            throw new NullPointerException();

        this.min = min;
        this.len.set(max).subtractLocal(min);
    }
    
    public void getRandomPoint(Vector3f store) {
        store.x = min.x + len.x * FastMath.nextRandomFloat();
        store.y = min.y + len.y * FastMath.nextRandomFloat();
        store.z = min.z + len.z * FastMath.nextRandomFloat();
    }

    public void write(G3DExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(min, "min", null);
        oc.write(len, "length", null);
    }
    public void read(G3DImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        min = (Vector3f) ic.readSavable("min", null);
        len = (Vector3f) ic.readSavable("length", null);
    }

}
