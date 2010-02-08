package com.g3d.light;

import com.g3d.bounding.BoundingVolume;
import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import com.g3d.math.ColorRGBA;
import com.g3d.math.Vector3f;
import com.g3d.scene.Spatial;
import java.io.IOException;

/**
 * Represents a point light.
 * A point light emits light from a given position into all directions in space.
 * E.g a lamp or a bright effect.
 */
public class PointLight extends Light {

    protected Vector3f position = new Vector3f();
    protected float radius = 0;

    @Override
    public void computeLastDistance(Spatial owner) {
        if (owner.getWorldBound() != null){
            BoundingVolume bv = owner.getWorldBound();
            lastDistance = bv.distanceSquaredTo(position);
        }else{
            lastDistance = owner.getWorldTranslation().distanceSquared(position);
        }
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position){
        this.position.set(position);
    }

    public float getRadius(){
        return radius;
    }

    public void setRadius(float radius){
        this.radius = radius;
    }

    @Override
    public Light.Type getType() {
        return Light.Type.Point;
    }

    @Override
    public void write(G3DExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(position, "position", null);
        oc.write(radius, "radius", 0f);
    }

    @Override
    public void read(G3DImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        position = (Vector3f) ic.readSavable("position", null);
        radius = ic.readFloat("radius", 0f);
    }

}
