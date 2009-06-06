package com.g3d.light;

import com.g3d.bounding.BoundingVolume;
import com.g3d.math.Vector3f;
import com.g3d.scene.Spatial;

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

}
