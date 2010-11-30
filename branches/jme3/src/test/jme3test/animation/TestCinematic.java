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

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.cinematic.Cinematic;
import com.jme3.animation.LoopMode;
import com.jme3.cinematic.MotionTrack;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.SoundTrack;
import com.jme3.app.SimpleApplication;
import com.jme3.cinematic.AbstractCinematicEvent;
import com.jme3.cinematic.AnimationTrack;
import com.jme3.cinematic.PlayState;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.PssmShadowRenderer;
import de.lessvoid.nifty.Nifty;

public class TestCinematic extends SimpleApplication {

    private Spatial model;
    private MotionPath path;
    private MotionTrack cameraMotionTrack;
    private AnimChannel channel;
    private Cinematic cinematic;
    private ChaseCamera chaseCam;
    private CameraNode camNode;

    public static void main(String[] args) {
        TestCinematic app = new TestCinematic();
        app.start();

    }

    @Override
    public void simpleInitApp() {
        createScene();

        cinematic = new Cinematic(rootNode);
        cinematic.setInitalDuration(20);
        stateManager.attach(cinematic);

        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        final BitmapText text = new BitmapText(guiFont, false);
        text.setSize(guiFont.getCharSet().getRenderedSize());
        text.setText("Press enter to play/pause cinematic");
        guiNode.attachChild(text);
        text.setLocalTranslation((cam.getWidth() - text.getLineWidth()) / 2, cam.getHeight(), 0);


        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager,
                inputManager,
                audioRenderer,
                guiViewPort);
        Nifty nifty = niftyDisplay.getNifty();

        nifty.fromXmlWithoutStartScreen("jme3test/animation/subtitle.xml");


        // attach the nifty display to the gui view port as a processor
        guiViewPort.addProcessor(niftyDisplay);

        createCameraMotion();

        cinematic.activateCamera(0, "aroundCam");
        cinematic.addCinematicEvent(0, cameraMotionTrack);
        cinematic.addCinematicEvent(0, new SoundTrack("Sound/Environment/Nature.ogg"));
        cinematic.addCinematicEvent(3, new SoundTrack("Sound/Effects/kick.wav"));
        SoundTrack beep = new SoundTrack("Sound/Effects/Beep.ogg");
        beep.setInitalDuration(1);
        cinematic.addCinematicEvent(5.0f, beep);
        cinematic.addCinematicEvent(3, new SubtitleTrack(nifty, "start", 3, "jMonkey engine really kicks A..."));
        cinematic.activateCamera(6, "topView");

        cinematic.activateCamera(8, "aroundCam");
        cinematic.activateCamera(9, "topView");
        cinematic.activateCamera(9.5f, "aroundCam");
        cinematic.activateCamera(10f, "topView");
        cinematic.activateCamera(10.5f, "aroundCam");
        cinematic.addCinematicEvent(6, new AnimationTrack(channel, "Walk"));

        cinematic.addCinematicEvent(6, new AbstractCinematicEvent() {

            @Override
            public void onPlay() {
            }

            @Override
            public void onUpdate(float tpf) {
                model.rotate(new Quaternion().fromAngleAxis(tpf * this.speed, Vector3f.UNIT_Y));
            }

            @Override
            public void onStop() {
            }

            @Override
            public void onPause() {
            }
        });


        flyCam.setEnabled(false);
        chaseCam = new ChaseCamera(cam, model, inputManager);
     //   chaseCam.setEnabled(true);
        initInputs();
     
    }

    private void createCameraMotion() {

        CameraNode node2 = cinematic.bindCamera("topView", cam);
        node2.setLocalTranslation(new Vector3f(0, 30, 0));
        node2.lookAt(model.getLocalTranslation(), Vector3f.UNIT_Y);

        camNode = cinematic.bindCamera("aroundCam", cam);



        path = new MotionPath();
        path.setCycle(true);
        path.addWayPoint(new Vector3f(20, 3, 0));
        path.addWayPoint(new Vector3f(0, 3, 20));
        path.addWayPoint(new Vector3f(-20, 3, 0));
        path.addWayPoint(new Vector3f(0, 3, -20));
        path.setCurveTension(0.83f);
        cameraMotionTrack = new MotionTrack(camNode, path);
        cameraMotionTrack.setLoopMode(LoopMode.Loop);
        cameraMotionTrack.setLookAt(model.getWorldTranslation(), Vector3f.UNIT_Y);
        cameraMotionTrack.setDirectionType(MotionTrack.Direction.LookAt);

    }

    private void createScene() {

        model = (Spatial) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        System.out.println("MODEL NAME : " + model.getName());
        model.center();

        model.setShadowMode(ShadowMode.CastAndReceive);

        AnimControl control = model.getControl(AnimControl.class);
        channel = control.createChannel();

        rootNode.attachChild(model);
        Material matSoil = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matSoil.setBoolean("m_UseMaterialColors", true);
        matSoil.setColor("m_Ambient", ColorRGBA.Gray);
        matSoil.setColor("m_Diffuse", ColorRGBA.Green);
        matSoil.setColor("m_Specular", ColorRGBA.Black);

        Geometry soil = new Geometry("soil", new Box(new Vector3f(0, -6.0f, 0), 50, 1, 50));
        soil.setMaterial(matSoil);
        soil.setShadowMode(ShadowMode.Receive);
        rootNode.attachChild(soil);
        DirectionalLight light = new DirectionalLight();
        light.setDirection(new Vector3f(0, -1, -1).normalizeLocal());
        light.setColor(ColorRGBA.White.mult(1.5f));
        rootNode.addLight(light);

        PssmShadowRenderer pssm = new PssmShadowRenderer(assetManager, 512, 1);
        pssm.setDirection(new Vector3f(0, -1, -1).normalizeLocal());
        pssm.setShadowIntensity(0.4f);
        viewPort.addProcessor(pssm);

    }

    private void initInputs() {
        inputManager.addMapping("togglePause", new KeyTrigger(keyInput.KEY_RETURN));

        ActionListener acl = new ActionListener() {

            public void onAction(String name, boolean keyPressed, float tpf) {
                if (name.equals("togglePause") && keyPressed) {
                    if (cinematic.getPlayState() == PlayState.Playing) {
                        cinematic.pause();
                        chaseCam.setEnabled(true);
                    } else {
                        chaseCam.setEnabled(false);
                        cinematic.play();
                    }
                }

            }
        };

        inputManager.addListener(acl, "togglePause");

    }


}
