package com.jme3.effect;

import com.jme3.effect.ParticleMesh.Type;
import com.jme3.export.G3DExporter;
import com.jme3.export.G3DImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.util.TempVars;
import java.io.IOException;
import java.util.ArrayList;

public class ParticleEmitter extends Geometry {

    private static final EmitterShape DEFAULT_SHAPE = new EmitterPointShape(Vector3f.ZERO);

    private EmitterShape shape = DEFAULT_SHAPE;
    private ParticleMesh particleMesh;
    private ParticleMesh.Type meshType;
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
    private Vector3f startVel = new Vector3f();

    private int imagesX = 1;
    private int imagesY = 1;

    private ColorRGBA startColor = new ColorRGBA(0.4f,0.4f,0.4f,0.5f);
    private ColorRGBA endColor = new ColorRGBA(0.1f,0.1f,0.1f,0.0f);
    private float startSize = 0.2f;
    private float endSize = 2f;
    private boolean worldSpace = false;

    public ParticleEmitter(String name, Type type, int numParticles){
        super(name);

        // particles neither recieve nor cast shadows
        setShadowMode(ShadowMode.Off);

        // particles are usually transparent
        setQueueBucket(Bucket.Transparent);

        meshType = type;

        setNumParticles(numParticles);
    }

    public ParticleEmitter(){
        super();
    }

    public void setShape(EmitterShape shape) {
        this.shape = shape;
    }

    // TODO: Remove dependency on a camera.
    // This should be in some updateRender() method
    // called by RenderManager
    public void setCamera(Camera cam){
        this.cam = cam;
    }

    public int getNumVisibleParticles(){
        return unusedIndices.size() + next;
    }

    /**
     * @param numParticles The maximum amount of particles that
     * can exist at the same time with this emitter.
     * Calling this method many times is not recommended.
     */
    public final void setNumParticles(int numParticles){
        particles = new Particle[numParticles];
        for (int i = 0; i < numParticles; i++){
            particles[i] = new Particle();
        }
    }

    public ColorRGBA getEndColor() {
        return endColor;
    }

    public void setEndColor(ColorRGBA endColor) {
        this.endColor.set(endColor);
    }

    public float getEndSize() {
        return endSize;
    }

    public void setEndSize(float endSize) {
        this.endSize = endSize;
    }

    public float getGravity() {
        return gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public float getHighLife() {
        return highLife;
    }

    public void setHighLife(float highLife) {
        this.highLife = highLife;
    }

    public int getImagesX() {
        return imagesX;
    }

    public void setImagesX(int imagesX) {
        this.imagesX = imagesX;
    }

    public int getImagesY() {
        return imagesY;
    }

    public void setImagesY(int imagesY) {
        this.imagesY = imagesY;
    }

    public float getLowLife() {
        return lowLife;
    }

    public void setLowLife(float lowLife) {
        this.lowLife = lowLife;
    }

    public float getParticlesPerSec() {
        return particlesPerSec;
    }

    public void setParticlesPerSec(float particlesPerSec) {
        this.particlesPerSec = particlesPerSec;
    }

    public ColorRGBA getStartColor() {
        return startColor;
    }

    public void setStartColor(ColorRGBA startColor) {
        this.startColor.set(startColor);
    }

    public float getStartSize() {
        return startSize;
    }

    public void setStartSize(float startSize) {
        this.startSize = startSize;
    }

    public Vector3f getStartVel() {
        return startVel;
    }

    public void setStartVel(Vector3f startVel) {
        this.startVel.set(startVel);
    }

    public float getVariation() {
        return variation;
    }

    public void setVariation(float variation) {
        this.variation = variation;
    }

    public void write(G3DExporter ex) throws IOException{
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(shape, "shape", DEFAULT_SHAPE);
        oc.write(meshType, "meshType", ParticleMesh.Type.Triangle);
        oc.write(particles.length, "numParticles", 0);
        oc.write(particlesPerSec, "particlesPerSec", 0);
        oc.write(lowLife, "lowLife", 0);
        oc.write(highLife, "highLife", 0);
        oc.write(gravity, "gravity", 0);
        oc.write(variation, "variation", 0);
        oc.write(imagesX, "imagesX", 1);
        oc.write(imagesY, "imagesY", 1);

        oc.write(startVel, "startVel", null);
        oc.write(startColor, "startColor", null);
        oc.write(endColor, "endColor", null);
        oc.write(startSize, "startSize", 0);
        oc.write(endSize, "endSize", 0);
        oc.write(worldSpace, "worldSpace", false);
    }

    public void read(G3DImporter im) throws IOException{
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        shape = (EmitterShape) ic.readSavable("shape", DEFAULT_SHAPE);
        meshType = ic.readEnum("meshType", ParticleMesh.Type.class, ParticleMesh.Type.Triangle);
        int numParticles = ic.readInt("numParticles", 0);
        setNumParticles(numParticles);

        particlesPerSec = ic.readFloat("particlesPerSec", 0);
        lowLife = ic.readFloat("lowLife", 0);
        highLife = ic.readFloat("highLife", 0);
        gravity = ic.readFloat("gravity", 0);
        variation = ic.readFloat("variation", 0);
        imagesX = ic.readInt("imagesX", 1);
        imagesY = ic.readInt("imagesY", 1);

        startVel = (Vector3f) ic.readSavable("startVel", null);
        startColor = (ColorRGBA) ic.readSavable("startColor", null);
        endColor = (ColorRGBA) ic.readSavable("endColor", null);
        startSize = ic.readFloat("startSiz", 0);
        endSize = ic.readFloat("endSize", 0);
        worldSpace = ic.readBoolean("worldSpace", false);
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
        shape.getRandomPoint(p.position);
        p.velocity.set(startVel);

        assert TempVars.get().lock();
        Vector3f temp = TempVars.get().vect1;
        temp.set(FastMath.nextRandomFloat(),FastMath.nextRandomFloat(),FastMath.nextRandomFloat());
        temp.multLocal(2f);
        temp.subtractLocal(1f,1f,1f);
        p.velocity.interpolate(temp, variation);
        assert TempVars.get().unlock();
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
        assert TempVars.get().lock();
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

            float b = (p.startlife - p.life) / p.startlife;
            p.color.interpolate(startColor, endColor, b);
            p.size = FastMath.interpolateLinear(b, startSize, endSize);

            p.imageIndex = (int) (b * imagesX * imagesY);
        }

        assert TempVars.get().unlock();

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
    public void updateLogicalState(float tpf){
        if (particleMesh == null){
            switch (meshType){
                case Point:
                    particleMesh = new ParticlePointMesh();
                    setMesh(particleMesh);
                    break;
                case Triangle:
                    particleMesh = new ParticleTriMesh();
                    setMesh(particleMesh);
                    break;
                default:
                    throw new IllegalStateException("Unrecognized particle type: "+meshType);
            }
            // create it
            particleMesh.initParticleData(particles.length, imagesX, imagesY);
        }

        updateParticleState(tpf);
        particleMesh.updateParticleData(particles, cam);
        
        // update the bounding volume to contain new positions;
        updateModelBound();
    }

}
