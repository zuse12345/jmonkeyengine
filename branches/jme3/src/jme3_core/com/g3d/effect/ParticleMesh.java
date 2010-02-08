package com.g3d.effect;

import com.g3d.renderer.Camera;
import com.g3d.scene.Mesh;

public abstract class ParticleMesh extends Mesh {

    public static enum Type {
        Point,
        Line,
        Triangle
    }

    public abstract void initParticleData(int numParticles, int imagesX, int imagesY);
    public abstract void updateParticleData(Particle[] particles, Camera cam);

}
