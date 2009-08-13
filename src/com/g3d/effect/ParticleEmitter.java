package com.g3d.effect;

import com.g3d.effect.ParticleMesh.Type;
import com.g3d.math.ColorRGBA;
import com.g3d.math.FastMath;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.queue.RenderQueue.Bucket;
import com.g3d.renderer.queue.RenderQueue.ShadowMode;
import com.g3d.scene.Geometry;
import com.g3d.util.TempVars;
import java.util.ArrayList;

public class ParticleEmitter extends Geometry {

    private ParticleMesh particleMesh;
    private Particle[] particles;
    private int next = 0;
    private ArrayList<Integer> unusedIndices = new ArrayList<Integer>();

    private Camera cam;
    private float particlesPerSec = 20;
    private float emitCarry = 0f;
    private float lowLife  = 3f;
    private float highLife = 7f;
    private float gravity = 0.1f;
    private float variation = 0.2f;

    private int imagesX = 1;
    private int imagesY = 1;

    private ColorRGBA startColor = new ColorRGBA(0.4f,0.4f,0.4f,0.5f);
    private ColorRGBA endColor = new ColorRGBA(0.1f,0.1f,0.1f,0.0f);
    private float startSize = 0.2f;
    private float endSize = 2f;

    public ParticleEmitter(String name, Type type, int numParticles, int imagesX, int imagesY){
        super(name);

        // particles neither recieve nor cast shadows
        setShadowMode(ShadowMode.Off);

        // particles are usually transparent
        setQueueBucket(Bucket.Transparent);

        switch (type){
            case Point:
                particleMesh = new ParticlePointMesh();
                setMesh(particleMesh);
                break;
            case Triangle:
                particleMesh = new ParticleTriMesh();
                setMesh(particleMesh);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized particle type: "+type);
        }

        particles = new Particle[numParticles];
        for (int i = 0; i < numParticles; i++){
            particles[i] = new Particle();
        }

        particleMesh.initParticleData(numParticles, imagesX, imagesY);
        this.imagesX = imagesX;
        this.imagesY = imagesY;
    }

    public void setCamera(Camera cam){
        this.cam = cam;
    }

    private int availableParticles(){
        return unusedIndices.size() + (particles.length - next);
    }

    private int newIndex(){
        if (unusedIndices.size() > 0)
            return unusedIndices.remove(0);
        else
            return next++;
    }

    private void freeIndex(int index){
        if (index == next-1)
            next--;
        else
            unusedIndices.add(index);
    }

    private void emitParticle(){
        int idx = newIndex();
        if (idx >= particles.length)
            return;

        Particle p = particles[idx];
//        p.imageIndex = (FastMath.nextRandomInt(0, imagesY-1) * imagesX) + FastMath.nextRandomInt(0, imagesX-1);
        p.startlife = lowLife + FastMath.nextRandomFloat() * (highLife - lowLife);
        p.life = p.startlife;
        p.color.set(startColor);
        p.size = startSize;
        p.position.set(0,0,0);
        p.velocity.set(0,2,0);

        Vector3f temp = TempVars.get().vect1;
        temp.set(FastMath.nextRandomFloat(),FastMath.nextRandomFloat(),FastMath.nextRandomFloat());
        temp.multLocal(2f);
        temp.subtractLocal(1f,1f,1f);
        p.velocity.interpolate(temp, variation);
    }

    private void freeParticle(int idx){
        Particle p = particles[idx];
        p.life = 0;
        p.size = 0f;
        p.color.set(0,0,0,0);
        p.imageIndex = 0;
        freeIndex(idx);
    }

    private void updateParticleState(float tpf){
        Vector3f temp = TempVars.get().vect1;

        for (int i = 0; i < particles.length; i++){
            Particle p = particles[i];
            if (p.life == 0) // particle is dead
                continue;

            p.life -= tpf;
            if (p.life <= 0)
                freeParticle(i);
            
            // position += velocity * tpf
            float g = gravity * tpf;
            p.velocity.y -= g;
            temp.set(p.velocity).multLocal(tpf);
            p.position.addLocal(temp);

            if (p.position.y < -3){
                // deflect
                Vector3f N  = new Vector3f(0,1,0);
                Vector3f N2 = new Vector3f(0,2,0);
                Vector3f I = p.velocity;
                Vector3f I2 = I.mult(2f);

                float IdotN = N.dot(I);

                I2.multLocal(IdotN).subtractLocal(N);
                
                p.velocity.set(N2);
                p.position.y = -2.9f;
//                System.out.println();
            }

            float b = (p.startlife - p.life) / p.startlife;
            p.color.interpolate(startColor, endColor, b);
            p.size = FastMath.interpolateLinear(b, startSize, endSize);

            p.imageIndex = (int) (b * imagesX * imagesY);
        }

        float particlesToEmitF = particlesPerSec * tpf;
        int particlesToEmit = (int) (particlesToEmitF);
        emitCarry += particlesToEmitF - particlesToEmit;
        
        if (emitCarry > 1f){
            particlesToEmit ++;
            emitCarry = 0f;
        }

        for (int i = 0; i < particlesToEmit; i++){
            emitParticle();
        }
    }

    @Override
    public void updateGeometricState(float tpf, boolean initiator){
        super.updateGeometricState(tpf, initiator);
        updateParticleState(tpf);
        particleMesh.updateParticleData(particles, cam);
        
        // update the bounding volume to contain new positions;
//        updateModelBound();
    }

}
