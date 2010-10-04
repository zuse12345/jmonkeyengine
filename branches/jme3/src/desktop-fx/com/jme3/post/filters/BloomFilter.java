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
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import java.util.ArrayList;

/**
 *
 * @author Nehon
 */
public class BloomFilter extends Filter{

    private Pass pass32=new Pass();
    private Pass pass64=new Pass();
    private Pass pass128=new Pass();
    private Pass pass256=new Pass();
    private MinFilter fbMinFilter = MinFilter.BilinearNoMipMaps;
    private MagFilter fbMagFilter = MagFilter.Bilinear;
    private int screenWidth;
    private int screenHeight;
    
    public BloomFilter(int width,int height) {
        super("Bloom");
        screenWidth=width;
        screenHeight=height;
    }

    @Override
    public void initMaterial(AssetManager manager) {
       material = new Material(manager, "Common/MatDefs/Post/BloomFinal.j3md");
       postRenderPasses=new ArrayList<Pass>();

       initPass(manager, screenWidth/16,screenHeight/16, pass32);
       initPass(manager, screenWidth/8,screenHeight/8, pass64);
       initPass(manager, screenWidth/4,screenHeight/4, pass128);
       initPass(manager,screenWidth/2,screenHeight/2, pass256);

    }

    private void initPass(AssetManager manager,int width,int height,Pass pass) {
        Material postMaterial = new Material(manager, "Common/MatDefs/Post/Bloom.j3md");
        postMaterial.setFloat("m_Size", height);
        pass.init(width, height, Format.RGBA8, Format.Depth, postMaterial);
        pass.getRenderedTexture().setMagFilter(fbMagFilter);
        pass.getRenderedTexture().setMinFilter(fbMinFilter);
        postRenderPasses.add(pass);

    }

    @Override
    public Material getMaterial() {
        material.setTexture("m_Tex32", pass32.getRenderedTexture());
        material.setTexture("m_Tex64", pass64.getRenderedTexture());
        material.setTexture("m_Tex128", pass128.getRenderedTexture());
        material.setTexture("m_Tex256", pass256.getRenderedTexture());
        return material;
    }

    @Override
    public void preRender(RenderManager renderManager, ViewPort viewPort) {

    }


}
