package com.g3d.control;

import com.g3d.math.Transform;

public class KeyFrame {

    Transform transform;
    float time;

    public KeyFrame(Transform transform, float time) {
        this.transform = transform;
        this.time = time;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public Transform getTransform() {
        return transform;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }
    
}
