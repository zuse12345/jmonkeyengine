package com.g3d.effect;

import com.g3d.math.Vector3f;

public class EmitterPointShape implements EmitterShape {

    private final Vector3f point;

    public EmitterPointShape(Vector3f point){
        this.point = point;
    }

    public void getRandomPoint(Vector3f store) {
       store.set(point);
    }

}
