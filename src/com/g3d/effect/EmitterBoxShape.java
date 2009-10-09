package com.g3d.effect;

import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import com.g3d.math.FastMath;
import com.g3d.math.Vector3f;
import java.io.IOException;

public class EmitterBoxShape implements EmitterShape {

    private Vector3f min, len;

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
