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

package jme3tools.preview;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.app.Application;
import com.jme3.scene.Spatial;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModelAnimHandler implements IAnimationHandler {

    private Spatial model;
    private AnimControl control;
    private AnimChannel channel;
    private Application app;

    public void setApp(Application app){
        this.app = app;
    }

    public ModelAnimHandler(Spatial model){
        this.model = model;
        control = model.getControl(AnimControl.class);
        channel = control.createChannel();
    }

    public Collection<String> list() {
        try{
            Collection<String> anims = (Collection<String>) app.enqueue(new Callable<Collection<String>>(){
            public Collection<String> call() throws Exception {
                return control.getAnimationNames();
            }
        }).get();
            return anims;
        }catch (InterruptedException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }catch (ExecutionException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public float getLength(final String name) {
        try{
            float len = app.enqueue(new Callable<Float>() {
                public Float call() throws Exception {
                    return control.getAnimationLength(name);
                }
            }).get();
            return len;
        }catch (InterruptedException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }catch (ExecutionException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    public void play(final String name) {
        blendTo(name, 0);
    }

    public void blendTo(final String name, final float time) {
        app.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                channel.setAnim(name, time);
                return null;
            }
        });
    }

    public void setSpeed(final float speed) {
        app.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                channel.setSpeed(speed);
                return null;
            }
        });
    }

    public float getSpeed() {
        return 1.0f;
    }

    public String getCurrent() {
        try{
            return app.enqueue(new Callable<String>() {
                public String call() throws Exception {
                    return channel.getAnimationName();
                }
            }).get();
        }catch (InterruptedException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }catch (ExecutionException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
