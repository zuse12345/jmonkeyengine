package com.g3d.audio;

import com.g3d.math.Vector3f;

public class DirectionalAudioSource extends AudioSource {

    protected Vector3f direction = new Vector3f(0,0,1);
    protected float innerAngle = 360;
    protected float outerAngle = 360;

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

    public float getInnerAngle() {
        return innerAngle;
    }

    public void setInnerAngle(float innerAngle) {
        this.innerAngle = innerAngle;
    }

    public float getOuterAngle() {
        return outerAngle;
    }

    public void setOuterAngle(float outerAngle) {
        this.outerAngle = outerAngle;
    }
    
}
