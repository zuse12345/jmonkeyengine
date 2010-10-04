/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jme3.post.ssao;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.Filter;
import com.jme3.post.Filter.Pass;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.shader.VarType;
import com.jme3.texture.Image.Format;


/**
 *
 * @author nehon
 */
public class SSAOFilter extends Filter {

    private Pass normalPass;
    private Material normalMaterial;
    private Vector3f frustumCorner;
    private Vector2f frustumNearFar;
    private SSAOConfig config;
    private Vector2f[] samples={new Vector2f(1.0f,0.0f),new Vector2f(-1.0f,0.0f),new Vector2f(0.0f,1.0f),new Vector2f(0.0f,-1.0f)};

    public SSAOFilter(ViewPort vp) {
        this(vp, new SSAOConfig());
    }

    public SSAOFilter(ViewPort vp, SSAOConfig config) {
        this.config=config;
        normalPass = new Pass();
        setRequiresDepthTexture(true);
        frustumNearFar=new Vector2f();

        int screenWidth = vp.getCamera().getWidth();
        int screenHeight = vp.getCamera().getHeight();
        //Need to fix this for the moment only works for 45° FOV
        float farY = FastMath.tan((float) (FastMath.DEG_TO_RAD * 45.0 / 2.0)) * vp.getCamera().getFrustumFar();
        float farX = farY * (screenWidth / screenHeight);
        frustumCorner = new Vector3f(farX, farY, vp.getCamera().getFrustumFar());
        frustumNearFar.x=vp.getCamera().getFrustumNear();
        frustumNearFar.y=vp.getCamera().getFrustumFar();

    }

    @Override
    public void init(AssetManager manager, int width, int height) {
        super.init(manager, width, height);
        normalPass.init(width, height, Format.RGBA8, Format.Depth);
    }

    @Override
    protected Format getDefaultPassDepthFormat() {
        return Format.Depth;
    }

    @Override
    public void preRender(RenderManager renderManager, ViewPort viewPort) {
        Renderer r = renderManager.getRenderer();
        r.setFrameBuffer(normalPass.getRenderFrameBuffer());
        renderManager.getRenderer().clearBuffers(true, true, true);
        renderManager.setForcedMaterial(normalMaterial);
        renderManager.renderViewPortQueues(viewPort, false);
        renderManager.setForcedMaterial(null);
        renderManager.getRenderer().setFrameBuffer(viewPort.getOutputFrameBuffer());
//        RenderState state=new RenderState();
//        state.setAlphaTest(true);
//        state.setAlphaFallOff(0.9f);
//        renderManager.setForcedRenderState(state);

    }

    @Override
    public Material getMaterial() {
        material.setTexture("m_Normals", normalPass.getRenderedTexture());
        material.setVector3("frustumCorner", frustumCorner);
        material.setFloat("m_SampleRadius", config.getSampleRadius() );
        material.setFloat("m_Intensity", config.getIntensity());
        material.setFloat("m_Scale", config.getScale());
        material.setFloat("m_Bias", config.getBias());
        material.setBoolean("m_UseAo", config.isUseAo());
        material.setBoolean("m_UseOnlyAo", config.isUseOnlyAo());
        material.setVector2("m_FrustumNearFar",frustumNearFar);
        material.setParam("m_Samples",VarType.Vector2Array,samples);

 //
        return material;
    }

    @Override
    public void initMaterial(AssetManager manager) {
        material = new Material(manager, "Common/MatDefs/SSAO/ssao.j3md");
        normalMaterial = new Material(manager, "Common/MatDefs/SSAO/normal.j3md");
    }

    public SSAOConfig getConfig() {
        return config;
    }

    public void setConfig(SSAOConfig config) {
        this.config = config;
    }

    
}
