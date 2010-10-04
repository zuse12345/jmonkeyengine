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
import com.jme3.post.Filter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.shader.VarType;


/**
 *
 * @author nehon
 */
public class RadialBlurFilter extends Filter {

    private float sampleDist = 1.0f;
    private float sampleStrength = 2.2f;
    private float[] samples={-0.08f,-0.05f,-0.03f,-0.02f,-0.01f,0.01f,0.02f,0.03f,0.05f,0.08f};

    public RadialBlurFilter() {
        super("Radial blur");
    }

    public RadialBlurFilter( float sampleDist,float sampleStrength) {
        this();
        this.sampleDist=sampleDist;
        this.sampleStrength=sampleStrength;
    }

    @Override
    public Material getMaterial() {
             
        material.setFloat("m_SampleDist", sampleDist);
        material.setFloat("m_SampleStrength", sampleStrength);
        material.setParam("m_Samples",VarType.FloatArray, samples);
        
        return material;
    }

    @Override
    public void preRender( RenderManager renderManager,ViewPort viewPort) {

    }

    public float getSampleDist() {
        return sampleDist;
    }

    public void setSampleDist(float sampleDist) {
        this.sampleDist = sampleDist;
    }

    public float getSampleStrength() {
        return sampleStrength;
    }

    public void setSampleStrength(float sampleStrength) {
        this.sampleStrength = sampleStrength;
    }

    @Override
    public void initMaterial(AssetManager manager) {
        material = new Material(manager, "Common/MatDefs/Blur/RadialBlur.j3md");
    }



}
