package com.g3d.light;

import com.g3d.math.Vector3f;
import com.g3d.scene.Spatial;

public class DirectionalLight extends Light {

    protected Vector3f direction = new Vector3f(0f, -1f, 0f);

    @Override
    public void computeLastDistance(Spatial owner) {
        lastDistance = 0; // directional lights are always closest to their owner
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f dir){
        direction.set(dir);
    }

    @Override
    public Type getType() {
        return Type.Directional;
    }

}
