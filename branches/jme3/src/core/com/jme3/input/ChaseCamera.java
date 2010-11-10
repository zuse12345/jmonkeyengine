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
package com.jme3.input;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.io.IOException;

/**
 * A camera that follows a spatial and can turn around it by dragging the mouse
 * @author nehon
 */
public class ChaseCamera implements ActionListener, AnalogListener, Control {

    private Spatial target = null;

    private float minHeight = 0.00f;
    private float maxHeight = FastMath.PI / 2;
    private float minDistance = 1.0f;
    private float maxDistance = 40.0f;

    private float distance = 20;
    private float targetDistance = distance;
    private float distanceLerpFactor=0;
    private boolean zooming=false;
    private float zoomSpeed = 2f;
    private float zoomSensitivity = zoomSpeed*0.001f;
   
    private float rotationSpeed = 1.0f;

    private InputManager inputManager;
    private Vector3f initialUpVec;
    private float rotation = FastMath.HALF_PI;
    private float targetRotation = rotation;
    private float rotationLerpFactor=0;


    private boolean rotating=false;
    private boolean vRotating=false;
    private float vRotation = FastMath.PI / 6;
    private float targetVRotation = vRotation;
    private float vRotationLerpFactor=0;
    private float rotationSensitivity = rotationSpeed*0.002f;

    private boolean canRotate;

    private boolean enabled = true;
    private Camera cam = null;
    private Vector3f pos;
    private boolean smoothMotion = false;

    /**
     * Constructs the chase camera
     * @param cam the application camera
     * @param target the spatial to follow
     */
    public ChaseCamera(Camera cam, final Spatial target) {
        this.target = target;

        this.cam = cam;
        initialUpVec = cam.getUp().clone();
        computePosition();
        target.addControl(this);
        cam.setLocation(pos);
    }

    /**
     * Constructs the chase camera, and registers inputs
     * @param cam the application camera
     * @param target the spatial to follow
     * @param inputManager the inputManager of the application to register inputs
     */
    public ChaseCamera(Camera cam, final Spatial target, InputManager inputManager) {
        this(cam, target);
        registerWithInput(inputManager);

    }

    public void onAction(String name, boolean keyPressed, float tpf) {
        if (name.equals("toggleRotate") && enabled) {
            if (keyPressed) {
                canRotate = true;
                inputManager.setCursorVisible(false);
            } else {
                canRotate = false;
                inputManager.setCursorVisible(true);
            }
        }


    }

    boolean zoomin;
    public void onAnalog(String name, float value, float tpf) {
        if (name.equals("mouseLeft")) {
            rotateCamera(-value);
        } else if (name.equals("mouseRight")) {
            rotateCamera(value);
        } else if (name.equals("Up")) {
            vRotateCamera(value);
        } else if (name.equals("Down")) {
            vRotateCamera(-value);
        } else if (name.equals("ZoomIn")) {
            zoomCamera(value);
            if(zoomin==false){
                 distanceLerpFactor=0;
            }
            zoomin=true;
        } else if (name.equals("ZoomOut")) {
            zoomCamera(-value);
            if(zoomin==true){
                 distanceLerpFactor=0;
            }
            zoomin=false;
        }

    }

    /**
     * Registers inputs with the input manager
     * @param inputManager
     */
    public void registerWithInput(InputManager inputManager) {
        String[] inputs = {"toggleRotate", "Down", "Up", "mouseLeft", "mouseRight", "ZoomIn", "ZoomOut"};

        this.inputManager = inputManager;

        inputManager.addMapping("Down", new MouseAxisTrigger(1, true));
        inputManager.addMapping("Up", new MouseAxisTrigger(1, false));
        inputManager.addMapping("ZoomIn", new MouseAxisTrigger(2, true));
        inputManager.addMapping("ZoomOut", new MouseAxisTrigger(2, false));
        inputManager.addMapping("mouseLeft", new MouseAxisTrigger(0, true));
        inputManager.addMapping("mouseRight", new MouseAxisTrigger(0, false));
        inputManager.addMapping("toggleRotate", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("toggleRotate", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

        inputManager.addListener(this, inputs);


    }

    private void computePosition() {

        float hDistance = (distance) * FastMath.sin((FastMath.PI / 2) - vRotation);
        pos = new Vector3f(hDistance * FastMath.cos(rotation), (distance) * FastMath.sin(vRotation), hDistance * FastMath.sin(rotation));
        pos = pos.add(target.getWorldTranslation());
    }

    //rotate the camera around the target on the horizontal plane
    private void rotateCamera(float value) {
        if (!canRotate || !enabled) {
            return;
        }
        rotating=true;
        targetRotation += value * rotationSpeed;


    }

    //move the camera toward or away the target
    private void zoomCamera(float value) {
        if (!enabled) {
            return;
        }
      
        zooming=true;
        targetDistance += value * zoomSpeed;
        if (targetDistance > maxDistance) {
            targetDistance = maxDistance;
        }
        if (targetDistance < minDistance) {
            targetDistance = minDistance;
        }
        if ((targetVRotation < minHeight) && (targetDistance > (minDistance + 1.0f))) {


            targetVRotation = minHeight;
        }

    }

    //rotate the camera around the target on the vertical plane
    private void vRotateCamera(float value) {
        if (!canRotate || !enabled) {
            return;
        }
        vRotating=true;
        targetVRotation += value * rotationSpeed;
        if (targetVRotation > maxHeight) {
            targetVRotation = maxHeight;
        }
        if ((targetVRotation < minHeight) && (targetDistance > (minDistance + 1.0f))) {
            targetVRotation = minHeight;
        }
    }

    /**
     * Update the camera, should only be called internally
     */
    protected void updateCamera(float tpf) {
        if (enabled) {
            if (smoothMotion) {
                if(zooming){
                    distanceLerpFactor=Math.min(distanceLerpFactor+tpf*zoomSensitivity,1);
                    distance=FastMath.interpolateLinear(distanceLerpFactor, distance, targetDistance);
                    if(targetDistance+0.1f>=distance && targetDistance-0.1f<=distance){
                        zooming=false;
                        distanceLerpFactor=0;
                    }
                }

                if(rotating){
                    rotationLerpFactor=Math.min(rotationLerpFactor+tpf*rotationSensitivity,1);
                    rotation=FastMath.interpolateLinear(rotationLerpFactor, rotation, targetRotation);
                    if(targetRotation+0.01f>=rotation && targetRotation-0.01f<=rotation){
                        rotating=false;
                        rotationLerpFactor=0;
                    }
                }
                if(vRotating){
                    vRotationLerpFactor=Math.min(vRotationLerpFactor+tpf*rotationSensitivity,1);
                    vRotation=FastMath.interpolateLinear(vRotationLerpFactor, vRotation, targetVRotation);
                    if(targetVRotation+0.01f>=vRotation && targetVRotation-0.01f<=vRotation){
                        vRotating=false;
                        vRotationLerpFactor=0;
                    }
                }

                computePosition();
                cam.setLocation(pos);
            } else {
                vRotation=targetVRotation;
                rotation=targetRotation;
                distance=targetDistance;
                computePosition();
                cam.setLocation(pos);
            }
            cam.lookAt(target.getWorldTranslation(), initialUpVec);
        }
    }

    /**
     * Return the enabled/disabled state of the camera
     * @return true if the camera is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable the camera
     * @param enabled true to enable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            canRotate = false; // reset this flag in-case it was on before
        }
    }

    /**
     * Returns the max zoom distance of the camera (default is 40)
     * @return maxDistance
     */
    public float getMaxDistance() {
        return maxDistance;
    }

    /**
     * Sets the max zoom distance of the camera (default is 40)
     * @param maxDistance
     */
    public void setMaxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
    }

    /**
     * Returns the min zoom distance of the camera (default is 1)
     * @return minDistance
     */
    public float getMinDistance() {
        return minDistance;
    }

    /**
     * Sets the min zoom distance of the camera (default is 1)
     * @return minDistance
     */
    public void setMinDistance(float minDistance) {
        this.minDistance = minDistance;
    }

    /**
     * clone this camera for a spatial
     * @param spatial
     * @return
     */
    public Control cloneForSpatial(Spatial spatial) {
        ChaseCamera cc = new ChaseCamera(cam, spatial, inputManager);
        cc.setMaxDistance(getMaxDistance());
        cc.setMinDistance(getMinDistance());
        return cc;
    }

    /**
     * Sets the spacial for the camera control, should only be used internally
     * @param spatial
     */
    public void setSpatial(Spatial spatial) {
        target = spatial;
    }

    /**
     * update the camera control, should on ly be used internally
     * @param tpf
     */
    public void update(float tpf) {
        updateCamera(tpf);
    }

    /**
     * renders the camera control, should on ly be used internally
     * @param rm
     * @param vp
     */
    public void render(RenderManager rm, ViewPort vp) {
        //nothing to render
    }

    /**
     * Write the camera
     * @param ex the exporter
     * @throws IOException
     */
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(maxDistance, "maxDistance", 40);
        capsule.write(minDistance, "minDistance", 1);
    }

    /**
     * Read the camera
     * @param im
     * @throws IOException
     */
    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        maxDistance = ic.readFloat("maxDistance", 40);
        minDistance = ic.readFloat("minDistance", 1);
    }

    public float getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(float maxHeight) {
        this.maxHeight = maxHeight;
    }

    public float getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(float minHeight) {
        this.minHeight = minHeight;
    }

    public boolean isSmoothMotion() {
        return smoothMotion;
    }

    public void setSmoothMotion(boolean smoothMotion) {
        this.smoothMotion = smoothMotion;
    }
}
