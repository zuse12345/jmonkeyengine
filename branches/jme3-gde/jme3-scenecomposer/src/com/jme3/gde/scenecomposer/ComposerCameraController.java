/*
 *  Copyright (c) 2009-2010 jMonkeyEngine
 *  All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 * 
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 
 *  * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.scenecomposer;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.scene.nodes.JmeNode;
import com.jme3.input.InputManager;
import com.jme3.input.RawInputListener;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import java.util.concurrent.Callable;

/**
 *
 * @author normenhansen
 */
public class ComposerCameraController implements ActionListener, AnalogListener, RawInputListener, AppState {

    private boolean leftMouse, rightMouse, middleMouse;
    private float deltaX, deltaY, deltaZ, deltaWheel;
    private int mouseX = 0;
    private int mouseY = 0;
    private Quaternion rot = new Quaternion();
    private Vector3f vector = new Vector3f();
    private Vector3f focus = new Vector3f();
    private Camera cam;
    private Node rootNode;
    private JmeNode jmeRootNode;
    private InputManager inputManager;
    private SceneComposerTopComponent master;
    private boolean moved = false;
    private boolean movedR = false;
    private boolean checkClick = false;
    private boolean checkClickR = false;

    public ComposerCameraController(Camera cam, JmeNode rootNode) {
        this.cam = cam;
        this.jmeRootNode = rootNode;
        this.rootNode = rootNode.getLookup().lookup(Node.class);
        inputManager = SceneApplication.getApplication().getInputManager();
    }

    public void setMaster(SceneComposerTopComponent component) {
        this.master = component;
    }

    public void enable() {
        inputManager.addRawInputListener(this);
        inputManager.addListener(this, "MouseAxisX");
        inputManager.addListener(this, "MouseAxisY");
        inputManager.addListener(this, "MouseAxisX-");
        inputManager.addListener(this, "MouseAxisY-");
        inputManager.addListener(this, "MouseWheel");
        inputManager.addListener(this, "MouseWheel-");
        inputManager.addListener(this, "MouseButtonLeft");
        inputManager.addListener(this, "MouseButtonMiddle");
        inputManager.addListener(this, "MouseButtonRight");
        SceneApplication.getApplication().getStateManager().attach(this);
    }

    public void disable() {
        inputManager.removeRawInputListener(this);
        inputManager.removeListener(this);
        SceneApplication.getApplication().getStateManager().detach(this);
    }

    public void setCamFocus(final Vector3f focus) {
        SceneApplication.getApplication().enqueue(new Callable<Object>() {

            public Object call() throws Exception {
                doSetCamFocus(focus);
                return null;
            }
        });

    }

    public void doSetCamFocus(Vector3f focus) {
        this.focus.set(focus);
        cam.setLocation(focus.add(vector, cam.getLocation()));
    }

    /*
     * methods to move camera
     */
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

    private void panCamera(float left, float up, float vecAmount) {
        cam.getLeft().mult(left, vector);
        vector.scaleAdd(up, cam.getUp(), vector);
        if(vecAmount>0){
            vector.multLocal(cam.getLocation().distance(focus)*vecAmount);
        }
        cam.setLocation(cam.getLocation().add(vector));
        focus.addLocal(vector);
    }

    private void moveCamera(float forward) {
        cam.getDirection().mult(forward, vector);
        cam.setLocation(cam.getLocation().add(vector));
    }

    private void zoomCamera(float amount) {
        float dist = cam.getLocation().distance(focus);
        amount = dist - Math.max(0f, dist - amount);
        Vector3f loc = cam.getLocation().clone();
        loc.scaleAdd(amount, cam.getDirection(), loc);
        cam.setLocation(loc);
    }

    public void onAction(String string, boolean bln, float f) {
        if ("MouseButtonLeft".equals(string)) {
            if (bln) {
                leftMouse = true;
                moved = false;
            } else {
                leftMouse = false;
                if (!moved) {
                    checkClick = true;
                }
            }
        }
        if ("MouseButtonRight".equals(string)) {
            if (bln) {
                rightMouse = true;
                movedR = false;
            } else {
                rightMouse = false;
                if (!movedR) {
                    checkClickR = true;
                }
            }
        }
    }

    public void onAnalog(String string, float f1, float f) {
        if ("MouseAxisX".equals(string)) {
            moved = true;
            movedR = true;
            if (leftMouse) {
                rotateCamera(Vector3f.UNIT_Y, -f1 * 2.5f);
            }
            if (rightMouse) {
                panCamera(f1 * 2.5f, 0, 1);
            }
        } else if ("MouseAxisY".equals(string)) {
            moved = true;
            movedR = true;
            if (leftMouse) {
                rotateCamera(cam.getLeft(), -f1 * 2.5f);
            }
            if (rightMouse) {
                panCamera(0, -f1 * 2.5f, 1);
            }
        } else if ("MouseAxisX-".equals(string)) {
            moved = true;
            movedR = true;
            if (leftMouse) {
                rotateCamera(Vector3f.UNIT_Y, f1 * 2.5f);
            }
            if (rightMouse) {
                panCamera(-f1 * 2.5f, 0, 1);
            }
        } else if ("MouseAxisY-".equals(string)) {
            moved = true;
            movedR = true;
            if (leftMouse) {
                rotateCamera(cam.getLeft(), f1 * 2.5f);
            }
            if (rightMouse) {
                panCamera(0, f1 * 2.5f, 1);
            }
        } else if ("MouseWheel".equals(string)) {
            zoomCamera(.1f);
        } else if ("MouseWheel-".equals(string)) {
            zoomCamera(-.1f);
        }
    }

    public void onJoyAxisEvent(JoyAxisEvent jae) {
    }

    public void onJoyButtonEvent(JoyButtonEvent jbe) {
    }

    public void onMouseMotionEvent(MouseMotionEvent mme) {
        mouseX = mme.getX();
        mouseY = mme.getY();
    }

    public void onMouseButtonEvent(MouseButtonEvent mbe) {
    }

    public void onKeyEvent(KeyInputEvent kie) {
    }

    /**APPSTATE**/
    public void initialize(AppStateManager asm, Application aplctn) {
        appInit = true;
    }
    private boolean appInit = false;

    public boolean isInitialized() {
        return appInit;
    }

    public void stateAttached(AppStateManager asm) {
    }

    public void stateDetached(AppStateManager asm) {
    }

    public void update(float f) {
        if (checkClick) {
            CollisionResults results = new CollisionResults();
            Ray ray = new Ray();
            Vector3f pos = cam.getWorldCoordinates(new Vector2f(mouseX, mouseY), 0).clone();
            Vector3f dir = cam.getWorldCoordinates(new Vector2f(mouseX, mouseY), 0.3f).clone();
            dir.subtractLocal(pos).normalizeLocal();
            ray.setOrigin(pos);
            ray.setDirection(dir);
            rootNode.collideWith(ray, results);
            if (results == null) {
                return;
            }
            final CollisionResult result = results.getClosestCollision();
            java.awt.EventQueue.invokeLater(new Runnable() {

                public void run() {
                    if (result != null && result.getGeometry() != null) {
                        SceneApplication.getApplication().setSelectedNode(jmeRootNode.getChild(result.getGeometry()));
                    } else {
                        SceneApplication.getApplication().setSelectedNode(jmeRootNode);
                    }
                }
            });
            checkClick = false;
        }
        if (checkClickR) {
            CollisionResults results = new CollisionResults();
            Ray ray = new Ray();
            Vector3f pos = cam.getWorldCoordinates(new Vector2f(mouseX, mouseY), 0).clone();
            Vector3f dir = cam.getWorldCoordinates(new Vector2f(mouseX, mouseY), 0.3f).clone();
            dir.subtractLocal(pos).normalizeLocal();
            ray.setOrigin(pos);
            ray.setDirection(dir);
            rootNode.collideWith(ray, results);
            if (results == null) {
                return;
            }
            CollisionResult result = results.getClosestCollision();
            if (result != null) {
                master.doMoveCursor(result.getContactPoint());
            }
            checkClickR = false;
        }
    }

    public void render(RenderManager rm) {
    }

    public void cleanup() {
    }
}
