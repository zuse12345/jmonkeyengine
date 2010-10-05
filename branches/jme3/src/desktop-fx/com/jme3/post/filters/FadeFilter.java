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

/**
 *
 * @author Nehon
 * implemented from boxjar implementation
 * see http://jmonkeyengine.org/groups/graphics/forum/topic/newbie-question-general-fade-inout-effect/#post-105559
 */
public class FadeFilter extends Filter {

    private float value=1;
    private boolean playing=false;
    private float direction=1;
    private float duration=1;
 
    public FadeFilter() {
        super("Fade In/Out");
    }

    public FadeFilter(float duration) {
        super("Fade In/Out");
        this.duration=duration;
    }

    @Override
    public Material getMaterial() {
        material.setFloat("m_Value", value);
        return material;
    }

    @Override
    public void preRender(RenderManager renderManager, ViewPort viewPort) {}

    @Override
    public void initMaterial(AssetManager manager) {
        material = new Material(manager, "Common/MatDefs/Post/Fade.j3md");
    }

    @Override
    public void preFrame(float tpf) {
        if(playing){
            value+=tpf*direction/duration;

            if(direction>0 && value>1){
                value=1;
                playing=false;
            }
            if(direction<0 && value<0){
                value=0;
                playing=false;
            }
        }
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public void fadeIn(){       
        direction=1;
        playing=true;
    }

    public void fadeOut(){      
        direction=-1;
        playing=true;

    }

}