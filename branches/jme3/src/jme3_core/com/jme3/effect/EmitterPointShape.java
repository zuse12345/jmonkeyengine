package com.jme3.effect;

import com.jme3.export.G3DExporter;
import com.jme3.export.G3DImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Vector3f;
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
