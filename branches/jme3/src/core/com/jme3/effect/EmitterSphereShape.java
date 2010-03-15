package com.jme3.effect;

import com.jme3.export.G3DExporter;
import com.jme3.export.G3DImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import java.io.IOException;

public class EmitterSphereShape implements EmitterShape {

    private Vector3f center;
    private float radius;

    public EmitterSphereShape(){
    }

    public EmitterSphereShape(Vector3f center, float radius) {
        if (center == null)
            throw new NullPointerException();

        if (radius <= 0)
            throw new IllegalArgumentException("Radius must be greater than 0");

        this.center = center;
        this.radius = radius;
    }

    public void getRandomPoint(Vector3f store) {
        float t = FastMath.nextRandomFloat() * FastMath.TWO_PI;
        store.z = (FastMath.nextRandomFloat() * 2f) - 1f;
        float r = FastMath.sqrt(1f - store.z * store.z);
        store.x = FastMath.cos(t) * r;
        store.y = FastMath.sin(t) * r;
        store.multLocal(radius).addLocal(center);
    }

    public void write(G3DExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(center, "center", null);
        oc.write(radius, "radius", 0);
    }

    public void read(G3DImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        center = (Vector3f) ic.readSavable("center", null);
        radius = ic.readFloat("radius", 0);
    }
    
}
