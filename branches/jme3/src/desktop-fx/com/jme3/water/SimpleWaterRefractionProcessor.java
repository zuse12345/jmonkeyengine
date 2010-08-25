/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.water;

import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;

/**
 *
 * @author normenhansen
 */
public class SimpleWaterRefractionProcessor implements SceneProcessor{
    RenderManager rm;
    ViewPort vp;

    public void initialize(RenderManager rm, ViewPort vp) {
        this.rm = rm;
        this.vp = vp;
    }

    public void reshape(ViewPort vp, int w, int h) {
    }

    public boolean isInitialized() {
        return rm != null;
    }

    public void preFrame(float tpf) {
    }

    public void postQueue(RenderQueue rq) {
//        rm.getRenderer().setClipPlane(0, -1, 0, 0);
    }

    public void postFrame(FrameBuffer out) {
//        rm.getRenderer().clearClipPlane();
    }

    public void cleanup() {
//        rm.getRenderer().clearClipPlane();
    }
}
