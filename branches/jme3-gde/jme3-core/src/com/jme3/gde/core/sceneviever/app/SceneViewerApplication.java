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
package com.jme3.gde.core.sceneviever.app;

import com.jme3.app.Application;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.logging.JmeLogHandler;
import com.jme3.input.FlyByCamera;
import com.jme3.input.binding.BindingListener;
import com.jme3.light.PointLight;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.system.AppSettings;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupProvider;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * TODO:
 * - generalized way to access/use rootNode from other plugins
 * - Node tree creation from scenegraph
 * @author normenhansen
 */
public class SceneViewerApplication extends Application implements LookupProvider, LookupListener, BindingListener {

    private boolean leftMouse, rightMouse, middleMouse;
    private float deltaX, deltaY, deltaWheel;
    private PointLight camLight;
    private static SceneViewerApplication application;

    public static SceneViewerApplication getApplication() {
        if (application == null) {
            application = new SceneViewerApplication();
        }
        return application;
    }
    protected static Node rootNode = new Node("Root Node");
    protected static Node guiNode = new Node("Gui Node");
    protected float secondCounter = 0.0f;
    protected BitmapText fpsText;
    protected FlyByCamera flyCam;
    protected boolean showSettings = true;
    private Lookup.Result result;
    private JmeLogHandler logHandler = new JmeLogHandler();

    public SceneViewerApplication() {
        manager = ProjectAssetManager.getManager();
        AppSettings settings = new AppSettings(true);
//        settings.setVSync(true);
//        settings.setRenderer("JOGL");
        setSettings(settings);

        Logger.getLogger("com.jme3").addHandler(logHandler);

        setPauseOnLostFocus(false);

        //add listener for project selection
        result = Utilities.actionsGlobalContext().lookupResult(Project.class);
        result.addLookupListener(this);
    }

    private void loadFPSText() {
        BitmapFont font = manager.loadFont("cooper.fnt");

        fpsText = new BitmapText(font, false);
        fpsText.setSize(font.getCharSet().getRenderedSize());
        fpsText.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        fpsText.setText("Frames per second");
        guiNode.attachChild(fpsText);
    }

    @Override
    public void initialize() {
        super.initialize();

        // enable depth test and back-face culling for performance
        renderer.applyRenderState(RenderState.DEFAULT);

        camLight = new PointLight();
        camLight.setColor(ColorRGBA.Gray);
        rootNode.addLight(camLight);

        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);
        loadFPSText();
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);
        cam.setLocation(new Vector3f(0, 0, 10));

        inputManager.addTriggerListener(this);
        inputManager.registerMouseAxisBinding("MOUSE_X+", 0, false);
        inputManager.registerMouseAxisBinding("MOUSE_X-", 0, true);
        inputManager.registerMouseAxisBinding("MOUSE_Y+", 1, false);
        inputManager.registerMouseAxisBinding("MOUSE_Y-", 1, true);
        inputManager.registerMouseAxisBinding("MOUSE_W+", 2, false);
        inputManager.registerMouseAxisBinding("MOUSE_W-", 2, true);

        inputManager.registerMouseButtonBinding("MOUSE_LEFT", 0);
        inputManager.registerMouseButtonBinding("MOUSE_RIGHT", 1);
        inputManager.registerMouseButtonBinding("MOUSE_MIDDLE", 2);
    }

    @Override
    public void update() {
        if (speed == 0) {
            return;
        }

        super.update();
        float tpf = timer.getTimePerFrame();

        Vector3f temp = camLight.getPosition();
        temp.set(cam.getLeft()).multLocal(5.0f);
        temp.addLocal(cam.getLocation());
        camLight.setPosition(temp);

        secondCounter += tpf;
        int fps = (int) timer.getFrameRate();
        if (secondCounter >= 1.0f) {
            fpsText.setText("Frames per second: " + fps);
            secondCounter = 0.0f;
        }

        rootNode.updateLogicalState(tpf);
        guiNode.updateLogicalState(tpf);
        rootNode.updateGeometricState();
        guiNode.updateGeometricState();

        renderManager.render(tpf);
    }

    public void showModel(String name) {
        rootNode.detachAllChildren();
        Spatial model = manager.loadModel(name);
        if (model == null) {
            StatusDisplayer.getDefault().setStatusText("could not load model " + name);
        }
//        scaleAndCenter(model, 1.0f);
        rootNode.attachChild(model);
        notifySceneListeners();
    }

    private void notifySceneListeners(){
        for (Iterator<SceneViewerListener> it = listeners.iterator(); it.hasNext();) {
            SceneViewerListener sceneViewerListener = it.next();
            sceneViewerListener.rootNodeChanged(rootNode);
        }
    }

    private static Spatial scaleAndCenter(Spatial model, float size) {
        if (model != null) {
            model.updateGeometricState();

            BoundingVolume worldBound = model.getWorldBound();
            if (worldBound == null) {
                model.setModelBound(new BoundingBox());
                model.updateModelBound();
                model.updateGeometricState();
                worldBound = model.getWorldBound();
            }

            if (worldBound != null) { // check not still null (no geoms)
                Vector3f center = worldBound.getCenter();

                BoundingBox boundingBox = new BoundingBox(center, 0, 0, 0);
                boundingBox.mergeLocal(worldBound);

                Vector3f extent = boundingBox.getExtent(null);
                float maxExtent = Math.max(Math.max(extent.x, extent.y), extent.z);
                float height = extent.y;
                if (maxExtent != 0) {
                    model.setLocalScale(size / maxExtent);
                    Vector3f pos = center.negate().addLocal(0.0f, height / 2.0f, 0.0f); //.multLocal(model.getLocalScale().x);
                    model.setLocalTranslation(pos);
                    System.out.println("Model size: " + maxExtent);
                    System.out.println("Model position: " + center);
                }
            }
        }
        return model;
    }

    //TODO: Lookup for Application
    public Lookup createAdditionalLookup(Lookup baseContext) {
        return Lookups.fixed(getApplication());
    }

    //changes asset manager (not needed right now, AssetManager is static)
    public void resultChanged(LookupEvent ev) {
        Collection<Project> myResult = result.allInstances();
        for (Iterator<Project> it = myResult.iterator(); it.hasNext();) {
            Project project = it.next();
            final ProjectAssetManager pmanager = project.getLookup().lookup(ProjectAssetManager.class);
            if (pmanager != null) {
                StatusDisplayer.getDefault().setStatusText(System.currentTimeMillis() + " - set asset manager: " + pmanager);
                enqueue(new Callable<Object>() {

                    public Object call() throws Exception {
                        if (manager != pmanager.getManager()) {
                            rootNode.detachAllChildren();
                            manager = pmanager.getManager();
                        }
                        return null;
                    }
                });
                return;
            }
        }
    }
    
    private Quaternion rot = new Quaternion();
    private Vector3f vector = new Vector3f();
    private Vector3f focus = new Vector3f();

    private void rotateCamera(Vector3f axis, float amount) {
        if (axis.equals(cam.getLeft())) {
            float elevation = -FastMath.asin(cam.getDirection().y);
            amount = Math.min(Math.max(elevation + amount,
                    -FastMath.HALF_PI), FastMath.HALF_PI)
                    - elevation;
        }
        rot.fromAngleAxis(amount, axis);
        cam.getLocation().subtract(focus, vector);
        rot.mult(vector, vector);
        focus.add(vector, cam.getLocation());

        Quaternion curRot = cam.getRotation().clone();
        cam.setRotation(rot.mult(curRot));
    }

    private void panCamera(float left, float up) {
        cam.getLeft().mult(left, vector);
        vector.scaleAdd(up, cam.getUp(), vector);
        cam.setLocation(cam.getLocation().add(vector));
        focus.addLocal(vector);
    }

    private void zoomCamera(float amount) {
        float dist = cam.getLocation().distance(focus);
        amount = dist - Math.max(0f, dist - amount);
        Vector3f loc = cam.getLocation().clone();
        loc.scaleAdd(amount, cam.getDirection(), loc);
        cam.setLocation(loc);
    }

    public void onBinding(String binding, float value) {
        if (binding.equals("UPDATE")) {
            if (leftMouse) {
                rotateCamera(Vector3f.UNIT_Y, -deltaX * 5);
                rotateCamera(cam.getLeft(), -deltaY * 5);
            }
            if (deltaWheel != 0) {
                zoomCamera(deltaWheel * 10);
            }
            if (rightMouse) {
                panCamera(deltaX * 10, -deltaY * 10);
            }

            leftMouse = false;
            rightMouse = false;
            middleMouse = false;
            deltaX = 0;
            deltaY = 0;
            deltaWheel = 0;
        }

        if (binding.equals("MOUSE_LEFT")) {
            leftMouse = value > 0f;
        } else if (binding.equals("MOUSE_RIGHT")) {
            rightMouse = value > 0f;
        } else if (binding.equals("MOUSE_MIDDLE")) {
            middleMouse = value > 0f;
        } else if (binding.equals("MOUSE_X+")) {
            deltaX = value;
        } else if (binding.equals("MOUSE_X-")) {
            deltaX = -value;
        } else if (binding.equals("MOUSE_Y+")) {
            deltaY = value;
        } else if (binding.equals("MOUSE_Y-")) {
            deltaY = -value;
        } else if (binding.equals("MOUSE_W+")) {
            deltaWheel = value;
        } else if (binding.equals("MOUSE_W-")) {
            deltaWheel = -value;
        }
    }

    //TODO: replace with Lookup functionality
    private LinkedList<SceneViewerListener> listeners=new LinkedList<SceneViewerListener>();

    public void addSceneListener(SceneViewerListener listener){
        listeners.add(listener);
    }

    public void removeSceneListener(SceneViewerListener listener){
        listeners.remove(listener);
    }
}
