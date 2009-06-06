package com.g3d.light;

import com.g3d.math.Vector3f;
import com.g3d.scene.Spatial;

/**
 * A light coming from a certain direction in world space. E.g sun or moon light.
 */
public class DirectionalLight extends Light {

    protected Vector3f direction = new Vector3f(0f, -1f, 0f);

    @Override
    public void computeLastDistance(Spatial owner) {
        lastDistance = 0; // directional lights are always closest to their owner
    }

    /**
     * @return The direction of the light.
     */
    public Vector3f getDirection() {
        return direction;
    }

    /**
     * Sets the direction of the light.
     * @param dir
     */
    public void setDirection(Vector3f dir){
        direction.set(dir);
    }

    @Override
    public Type getType() {
        return Type.Directional;
    }

}
