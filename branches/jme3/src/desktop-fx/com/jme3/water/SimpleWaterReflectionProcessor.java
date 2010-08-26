/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.water;

import com.jme3.asset.AssetManager;
import com.jme3.material.RenderState;
import com.jme3.math.Quaternion;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.shader.DefineList;
import com.jme3.shader.Shader;
import com.jme3.shader.ShaderKey;
import com.jme3.shader.VarType;
import com.jme3.texture.FrameBuffer;

/**
 *
 * @author normenhansen
 */
public class SimpleWaterReflectionProcessor implements SceneProcessor {

    RenderManager rm;
    ViewPort vp;
    Shader clipShader;
    AssetManager manager;

    public SimpleWaterReflectionProcessor() {
    }

    public SimpleWaterReflectionProcessor(AssetManager manager) {
        this.manager = manager;
        DefineList defList=new DefineList();
        Quaternion vec=new Quaternion(0,1,0,0);
        ShaderKey key = new ShaderKey("Common/MatDefs/Water/clip_plane.vert", "Common/MatDefs/Water/clip_plane.frag", defList, "GLSL100");
        clipShader = manager.loadShader(key);
        clipShader.getUniform("u_clipPlane").setValue(VarType.Vector4, vec);
    }

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
        rm.getRenderer().setShader(clipShader);
    }

    public void postFrame(FrameBuffer out) {
    }

    public void cleanup() {
        rm.getRenderer().deleteShader(clipShader);
    }
}
