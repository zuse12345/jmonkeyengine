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
package com.jme3.post.filters;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.post.Filter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.Image.Format;
import java.util.ArrayList;

/**
 *
 * @author Nehon
 */
public class BloomFilter extends Filter {

    public static final int GLOW_MODE_ONLY_EXTRACTED_LIGHTS=0;
    public static final int GLOW_MODE_ONLY_GLOW_OBJECTS=1;
    public static final int GLOW_MODE_BOTH=2;

    private int glowMode=GLOW_MODE_ONLY_EXTRACTED_LIGHTS;

    //Bloom parameters
    private float blurScale = 1.5f;
    private float exposurePower = 5.0f;
    private float exposureCutOff = 0.0f;
    private float bloomIntensity = 2.0f;
 

    private Pass preGlowPass;
    private Pass extractPass;
    private Pass horizontalBlur = new Pass();
    private Pass verticalalBlur = new Pass();
    private Material extractMat;
    private Material vBlurMat;
    private Material hBlurMat;
    private int screenWidth;
    private int screenHeight;
    private ColorRGBA backupColor;

    public BloomFilter(int width, int height) {
        super("Bloom");
        screenWidth = width;
        screenHeight = height;
    }

    public BloomFilter(int width, int height, int glowMode) {
        this(width, height);
        this.glowMode=glowMode;

    }

    @Override
    public void initMaterial(AssetManager manager) {
         if(glowMode!=GLOW_MODE_ONLY_EXTRACTED_LIGHTS){
            preGlowPass=new Pass();
            preGlowPass.init(screenWidth, screenHeight, Format.RGBA8, Format.Depth);
        }

        postRenderPasses = new ArrayList<Pass>();
        //configuring extractPass
        extractMat = new Material(manager, "Common/MatDefs/Post/BloomExtract.j3md");
        extractPass = new Pass() {

            @Override
            public boolean requiresSceneAsTexture() {
                return true;
            }

            @Override
            public void beforeRender() {
                extractMat.setFloat("m_ExposurePow", exposurePower);
                extractMat.setFloat("m_ExposureCutoff", exposureCutOff);
                if(glowMode!=GLOW_MODE_ONLY_EXTRACTED_LIGHTS){
                    extractMat.setTexture("m_GlowMap", preGlowPass.getRenderedTexture());
                }
                extractMat.setBoolean("m_Extract", glowMode!=GLOW_MODE_ONLY_GLOW_OBJECTS);
            }
        };
        
        extractPass.init(screenWidth, screenHeight, Format.RGBA8, Format.Depth, extractMat);
        postRenderPasses.add(extractPass);

        //configuring horizontal blur pass
        hBlurMat = new Material(manager, "Common/MatDefs/Blur/HGaussianBlur.j3md");
        horizontalBlur = new Pass() {

            @Override
            public void beforeRender() {
                hBlurMat.setTexture("m_Texture", extractPass.getRenderedTexture());
                hBlurMat.setFloat("m_Size", screenWidth);
                hBlurMat.setFloat("m_Scale", blurScale);
            }
        };
        horizontalBlur.init(screenWidth, screenHeight, Format.RGBA8, Format.Depth, hBlurMat);
        postRenderPasses.add(horizontalBlur);

        //configuring vertical blur pass
        vBlurMat = new Material(manager, "Common/MatDefs/Blur/VGaussianBlur.j3md");
        verticalalBlur = new Pass() {

            @Override
            public void beforeRender() {
                vBlurMat.setTexture("m_Texture", horizontalBlur.getRenderedTexture());
                vBlurMat.setFloat("m_Size", screenHeight);
                vBlurMat.setFloat("m_Scale", blurScale);
            }
        };
        verticalalBlur.init(screenWidth, screenHeight, Format.RGBA8, Format.Depth, vBlurMat);
        postRenderPasses.add(verticalalBlur);


        //final material
        material = new Material(manager, "Common/MatDefs/Post/BloomFinal.j3md");
        material.setTexture("m_BloomTex", verticalalBlur.getRenderedTexture());

    }

    @Override
    public Material getMaterial() {
    
        material.setFloat("m_BloomIntensity", bloomIntensity);
        
        return material;
    }

    @Override
    public void preRender(RenderManager renderManager, ViewPort viewPort) {
         if(glowMode!=GLOW_MODE_ONLY_EXTRACTED_LIGHTS){
            backupColor=viewPort.getBackgroundColor();
            viewPort.setBackgroundColor(ColorRGBA.Black);
            renderManager.getRenderer().setFrameBuffer(preGlowPass.getRenderFrameBuffer());
            renderManager.getRenderer().clearBuffers(true, true, true);
            renderManager.setForcedTechnique("Glow");
            try{
                renderManager.renderViewPortQueues(viewPort, false);
            }
            catch(IllegalArgumentException iae){
                System.err.println("You need to add the Glow technique to your custom material");
                iae.printStackTrace();
            }
            viewPort.setBackgroundColor(backupColor);
            renderManager.setForcedTechnique(null);
            renderManager.getRenderer().setFrameBuffer(viewPort.getOutputFrameBuffer());
         
        }
    }

    public float getBloomIntensity() {
        return bloomIntensity;
    }

    /**
     *
     * intensity of the bloom effect
     * @param bloomIntensity
     */
    public void setBloomIntensity(float bloomIntensity) {
        this.bloomIntensity = bloomIntensity;
    }

    public float getBlurScale() {
        return blurScale;
    }

    /**
     * The spread of the bloom
     * @param blurScale
     */
    public void setBlurScale(float blurScale) {
        this.blurScale = blurScale;
    }

    public float getExposureCutOff() {
        return exposureCutOff;
    }

    /**
     * Define the color threshold on which the bloom will be applied (0.0 to 1.0)
     * @param exposureCutOff
     */
    public void setExposureCutOff(float exposureCutOff) {
        this.exposureCutOff = exposureCutOff;
    }

    public float getExposurePower() {
        return exposurePower;
    }

    /**
     * the power of the bloomed color
     * @param exposurePower
     */
    public void setExposurePower(float exposurePower) {
        this.exposurePower = exposurePower;
    }


}
