package com.g3d.effect;

import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.OutputCapsule;
import com.g3d.math.Vector3f;
import java.io.IOException;

public class EmitterPointShape implements EmitterShape {

    private Vector3f point;

    public EmitterPointShape(){
    }

    public EmitterPointShape(Vector3f point){
        this.point = point;
    }

    public void getRandomPoint(Vector3f store) {
       store.set(point);
    }

    public void write(G3DExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(point, "point", null);
    }

    public void read(G3DImporter im) throws IOException {
        this.point = (Vector3f) im.getCapsule(this).readSavable("point", null);
    }

}
