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
package com.jme3.gde.core.scene;

import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 *
 * @author normenhansen
 */
public class SceneCameraController implements ActionListener, AnalogListener {

    private boolean leftMouse, rightMouse, middleMouse;
    private float deltaX = 0.01f, deltaY = 0.01f, deltaZ = 0.01f, deltaWheel = 1;
    private float mouseX = 0;
    private float mouseY = 0;
    private Quaternion rot = new Quaternion();
    private Vector3f vector = new Vector3f();
    private Vector3f focus = new Vector3f();
    private Camera cam;
    private InputManager inputManager;

    public SceneCameraController(Camera cam, InputManager inputManager) {
        this.cam = cam;
        this.inputManager = inputManager;

        inputManager.addMapping("MouseAxisX", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("MouseAxisY", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("MouseAxisX-", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("MouseAxisY-", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping("MouseWheel", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("MouseWheel-", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        inputManager.addMapping("MouseButtonLeft", new MouseButtonTrigger(0));
        inputManager.addMapping("MouseButtonMiddle", new MouseButtonTrigger(1));
        inputManager.addMapping("MouseButtonRight", new MouseButtonTrigger(2));
    }

    public void enable() {
        inputManager.addListener(this, "MouseAxisX");
        inputManager.addListener(this, "MouseAxisY");
        inputManager.addListener(this, "MouseAxisX-");
        inputManager.addListener(this, "MouseAxisY-");
        inputManager.addListener(this, "MouseWheel");
        inputManager.addListener(this, "MouseWheel-");
        inputManager.addListener(this, "MouseButtonLeft");
        inputManager.addListener(this, "MouseButtonMiddle");
        inputManager.addListener(this, "MouseButtonRight");
    }

    public void disable() {
        inputManager.removeListener(this);
    }

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
            } else {
                leftMouse = false;
            }
        }
        if ("MouseButtonRight".equals(string)) {
            if (bln) {
                rightMouse = true;
            } else {
                rightMouse = false;
            }
        }
    }

    public void onAnalog(String string, float f, float f1) {
        if ("MouseAxisX".equals(string)) {
            //deltaX=f1*100.0f;
            mouseX=f;
            if (leftMouse) {
                rotateCamera(Vector3f.UNIT_Y, -f1 * 1);
            }
            if (rightMouse) {
                panCamera(deltaX * 10, -deltaY * 10);
            }
        }
        else if ("MouseAxisY".equals(string)) {
            mouseY=f;
            //deltaY=f1*100.0f;
            if (leftMouse) {
                rotateCamera(cam.getLeft(), -f1 * 1);
            }
            if (rightMouse) {
                panCamera(deltaX * 10, -deltaY * 10);
            }
        }
        else if ("MouseAxisX-".equals(string)) {
            //deltaX=f1*100.0f;
            mouseX=f;
            if (leftMouse) {
                rotateCamera(Vector3f.UNIT_Y, f1 * 1);
            }
            if (rightMouse) {
                panCamera(deltaX * 10, -deltaY * 10);
            }
        }
        else if ("MouseAxisY-".equals(string)) {
            mouseY=f;
            //deltaY=f1*100.0f;
            if (leftMouse) {
                rotateCamera(cam.getLeft(), f1 * 1);
            }
            if (rightMouse) {
                panCamera(deltaX * 10, -deltaY * 10);
            }
        }
        else if ("MouseWheel".equals(string)) {
            zoomCamera(.1f);
        }
        else if ("MouseWheel-".equals(string)) {
            zoomCamera(-.1f);
        }
    }
}
