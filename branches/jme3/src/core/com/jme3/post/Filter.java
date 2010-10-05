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

package com.jme3.post;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import java.util.List;

/**
 * Filter abstract class
 * Any Filter must extends this class
 * Holds a frameBuffer and a texture
 * The getMaterial must return a Material that use a GLSL shader immplementing the desired effect
 */
public abstract class Filter {

    private String name;
    private boolean requiresDepthTexture = false;
    protected Pass defaultPass = new Pass();
    protected List<Pass> postRenderPasses;
    protected Material material;

    public Filter(String name) {
        this.name = name;
    }

    public class Pass {

        protected FrameBuffer renderFrameBuffer;
        protected Texture2D renderedTexture;
        protected Material passMaterial;

        public void init(int width, int height, Format textureFormat, Format depthBurfferFormat) {
            renderFrameBuffer = new FrameBuffer(width, height, 0);
            renderedTexture = new Texture2D(width, height, textureFormat);
            renderFrameBuffer.setDepthBuffer(depthBurfferFormat);
            renderFrameBuffer.setColorTexture(renderedTexture);
        }

        public void init(int width, int height, Format textureFormat, Format depthBurfferFormat, Material material) {
            init(width, height, textureFormat, depthBurfferFormat);
            passMaterial = material;
        }

        public FrameBuffer getRenderFrameBuffer() {
            return renderFrameBuffer;
        }

        public void setRenderFrameBuffer(FrameBuffer renderFrameBuffer) {
            this.renderFrameBuffer = renderFrameBuffer;
        }

        public Texture2D getRenderedTexture() {
            return renderedTexture;
        }

        public void setRenderedTexture(Texture2D renderedTexture) {
            this.renderedTexture = renderedTexture;
        }

        public Material getPassMaterial() {
            return passMaterial;
        }

        public void setPassMaterial(Material passMaterial) {
            this.passMaterial = passMaterial;
        }
    }

    protected Format getDefaultPassTextureFormat() {
        return Format.RGB8;
    }

    protected Format getDefaultPassDepthFormat() {
        return Format.Depth;
    }

    public Filter() {
        this("filter");
    }

    public void init(AssetManager manager, int width, int height) {
        defaultPass.init(width, height, getDefaultPassTextureFormat(), getDefaultPassDepthFormat());
        initMaterial(manager);
    }

    public void cleanup(Renderer r) {
        if (defaultPass.renderFrameBuffer != null) {
            r.deleteFrameBuffer(defaultPass.renderFrameBuffer);
            r.deleteTexture(defaultPass.renderedTexture);
        }
    }

    public abstract void initMaterial(AssetManager manager);

    public abstract Material getMaterial();

    public abstract void preRender(RenderManager renderManager, ViewPort viewPort);

    public void preFrame(float tpf){
        
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FrameBuffer getRenderFrameBuffer() {
        return defaultPass.renderFrameBuffer;
    }

    public void setRenderFrameBuffer(FrameBuffer renderFrameBuffer) {
        this.defaultPass.renderFrameBuffer = renderFrameBuffer;
    }

    public Texture2D getRenderedTexture() {
        return defaultPass.renderedTexture;
    }

    public void setRenderedTexture(Texture2D renderedTexture) {
        this.defaultPass.renderedTexture = renderedTexture;
    }

    public boolean isRequiresDepthTexture() {
        return requiresDepthTexture;
    }

    public void setRequiresDepthTexture(boolean requiresDepthTexture) {
        this.requiresDepthTexture = requiresDepthTexture;
    }

    public List<Pass> getPostRenderPasses() {
        return postRenderPasses;
    }

    public void setPostRenderPasses(List<Pass> postRenderPasses) {
        this.postRenderPasses = postRenderPasses;
    }
}
