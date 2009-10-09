package com.g3d.effect;

import com.g3d.bounding.BoundingSphere;
import com.g3d.math.FastMath;
import com.g3d.math.Vector3f;

public class EmitterSphereShape implements EmitterShape {

    private final Vector3f center;
    private final float radius;

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
    
}
