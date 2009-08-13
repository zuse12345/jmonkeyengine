package com.g3d.effect;

import com.g3d.math.ColorRGBA;
import com.g3d.math.Vector3f;

public class Particle {

    final Vector3f velocity = new Vector3f();
    final Vector3f position = new Vector3f();
    final ColorRGBA color = new ColorRGBA(0,0,0,0);
    float size = 0f;
    float life;
    float startlife;
    float angle;
    int imageIndex = 0;

}
