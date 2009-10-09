package com.g3d.effect;

import com.g3d.export.Savable;
import com.g3d.math.Vector3f;

public interface EmitterShape extends Savable {
    public void getRandomPoint(Vector3f store);
}
