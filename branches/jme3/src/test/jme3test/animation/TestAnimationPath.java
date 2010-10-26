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
package jme3test.animation;

import com.jme3.animation.AnimationPath;
import com.jme3.app.SimpleApplication;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import java.util.ArrayList;

public class TestAnimationPath extends SimpleApplication {

    private Spatial teapot;
    private boolean active = true;
    private boolean playing=false;
    private AnimationPath path;

    public static void main(String[] args) {
        TestAnimationPath app = new TestAnimationPath();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        createScene();
        cam.setLocation(new Vector3f(8.4399185f, 11.189463f, 14.267577f));
        path = new AnimationPath(teapot);

//        ArrayList<Vector3f> test=new ArrayList<Vector3f>();
//
//        test.add(new Vector3f(10, 3, 0));
//        test.add(new Vector3f(10, 3, 10));
//        test.add(new Vector3f(-10, 3, 10));
//        test.add(new Vector3f(-10, 3, 0));
//
//
//        path.addControlPoint(test.get(0).subtract(test.get(1).subtract(test.get(0))));
//         path.addControlPoint(test.get(0));
//         path.addControlPoint(test.get(1));
//         path.addControlPoint(test.get(2));
//         path.addControlPoint(test.get(3));
//         path.addControlPoint(test.get(3).add(test.get(3).subtract(test.get(2))));
//
//        path.enableDebugShape(assetManager, rootNode);
        path = new AnimationPath(teapot);
        //path.setPathInterpolation(AnimationPath.PathInterpolation.Linear);
        path.addWayPoint(new Vector3f(10, 3, 0));
        path.addWayPoint(new Vector3f(10, 3, 10));
        path.addWayPoint(new Vector3f(-10, 3, 10));
        path.addWayPoint(new Vector3f(-10, 3, 0));
        path.addWayPoint(new Vector3f(-10, 8, 0));
        path.addWayPoint(new Vector3f(10, 8, 0));
        path.addWayPoint(new Vector3f(10, 8, 10));
        path.enableDebugShape(assetManager, rootNode);

        flyCam.setEnabled(false);
        ChaseCamera chaser=new ChaseCamera(cam, teapot);
        chaser.registerWithInput(inputManager);
        initInputs();

    }

    private void createScene() {
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setFloat("m_Shininess", 0.3f);
        mat.setBoolean("m_UseMaterialColors", true);
        mat.setColor("m_Ambient", ColorRGBA.Black);
        mat.setColor("m_Diffuse", ColorRGBA.Black);
        mat.setColor("m_Specular", ColorRGBA.White.mult(0.6f));
        Material matSoil = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matSoil.setFloat("m_Shininess", 15f);
        matSoil.setBoolean("m_UseMaterialColors", true);
        matSoil.setColor("m_Ambient", ColorRGBA.Gray);
        matSoil.setColor("m_Diffuse", ColorRGBA.Black);
        matSoil.setColor("m_Specular", ColorRGBA.White);
        teapot = assetManager.loadModel("Models/Teapot/Teapot.obj");
        teapot.setLocalScale(3);
        teapot.setMaterial(mat);
        teapot.setShadowMode(ShadowMode.CastAndReceive);
        rootNode.attachChild(teapot);
        Geometry soil = new Geometry("soil", new Box(new Vector3f(0, -1.0f, 0), 50, 1, 50));
        soil.setMaterial(matSoil);
        soil.setShadowMode(ShadowMode.CastAndReceive);
        rootNode.attachChild(soil);
        DirectionalLight light = new DirectionalLight();
        light.setDirection(new Vector3f(0, -1, 0).normalizeLocal());
        light.setColor(ColorRGBA.White.mult(1.5f));
        rootNode.addLight(light);
    }

    private void initInputs() {
        inputManager.addMapping("toggle", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("play_stop", new KeyTrigger(KeyInput.KEY_SPACE));
        ActionListener acl = new ActionListener() {

            public void onAction(String name, boolean keyPressed, float tpf) {
                if (name.equals("toggle") && keyPressed) {
                    if (active) {
                        active = false;
                        path.disableDebugShape();
                    } else {
                        active = true;
                        path.enableDebugShape(assetManager, rootNode);
                    }
                }
                if (name.equals("play_stop") && keyPressed) {
                    if (playing) {
                        playing = false;
                        path.stop();
                    } else {
                        playing = true;
                        path.play();
                    }                    
                }
            }
        };

        inputManager.addListener(acl, "toggle","play_stop");

    }
}
