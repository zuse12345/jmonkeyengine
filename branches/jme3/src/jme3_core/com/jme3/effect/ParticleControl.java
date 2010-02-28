package com.jme3.effect;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.ControlType;

public class ParticleControl extends AbstractControl {

    private final ParticleEmitter emitter;

    public ParticleControl(ParticleEmitter emitter){
        super(emitter);
        this.emitter = emitter;
    }

    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public ControlType getType() {
        return ControlType.Particle;
    }



}
