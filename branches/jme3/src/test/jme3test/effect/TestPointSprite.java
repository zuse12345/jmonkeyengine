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

package jme3test.effect;

import com.jme3.app.SimpleApplication;
import com.jme3.effect.EmitterBoxShape;
import com.jme3.effect.EmitterSphereShape;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL40;

public class TestPointSprite extends SimpleApplication {

    public static void main(String[] args){
        TestPointSprite app = new TestPointSprite();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        GL11.glEnable(GL20.GL_POINT_SPRITE);
        GL11.glTexEnvi(GL20.GL_POINT_SPRITE, GL20.GL_COORD_REPLACE, GL11.GL_TRUE);
        GL11.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
//        GL14.glPointParameterf( GL14.GL_POINT_FADE_THRESHOLD_SIZE, 60.0f );
        GL14.glPointParameterf( GL14.GL_POINT_SIZE_MIN, 1.0f );

        GL11.glEnable(GL13.GL_MULTISAMPLE);
        GL11.glEnable(GL13.GL_SAMPLE_ALPHA_TO_COVERAGE);

        ParticleEmitter emit = new ParticleEmitter("Emitter", Type.Point, 10000);
        emit.setShape(new EmitterBoxShape(new Vector3f(-1.8f, -1.8f, -1.8f),
                                          new Vector3f(1.8f, 1.8f, 1.8f)));
        emit.setGravity(0);
        emit.setLowLife(60);
        emit.setHighLife(60);
        emit.setStartVel(new Vector3f(0, 0, 0));
        emit.setImagesX(15);
        emit.setStartSize(0.05f);
        emit.setEndSize(0.05f);
        emit.setStartColor(ColorRGBA.White);
        emit.setEndColor(ColorRGBA.White);
        emit.setSelectRandomImage(true);
        emit.emitAllParticles();
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setBoolean("m_PointSprite", true);
        mat.setTexture("m_Texture", assetManager.loadTexture("Effects/Smoke/Smoke.png"));
        emit.setMaterial(mat);

        rootNode.attachChild(emit);
        
    }

}
