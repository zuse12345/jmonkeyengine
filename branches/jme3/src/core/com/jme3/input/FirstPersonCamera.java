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

import com.jme3.collision.MotionAllowedListener;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

@Deprecated
public class FirstPersonCamera {

    private Camera cam;
    private Vector3f initialUpVec;
    private Vector3f gravity = new Vector3f();
    private float rotationSpeed = 1f;
    private float moveSpeed = 3f;
    private boolean movedThisFrame = false;
    private MotionAllowedListener motionAllowed = null;

    /**
     * Creates a new FirstPersonCamera to control the given Camera object.
     * @param cam
     */
    public FirstPersonCamera(Camera cam, Vector3f gravity){
        this.cam = cam;
        initialUpVec = cam.getUp().clone();
        this.gravity.set(gravity);
    }

    public void setMotionAllowedListener(MotionAllowedListener listener){
        this.motionAllowed = listener;
    }

    /**
     * Sets the move speed. The speed is given in world units per second.
     * @param moveSpeed
     */
    public void setMoveSpeed(float moveSpeed){
        this.moveSpeed = moveSpeed;
    }

    /**
     * Sets the rotation speed.
     * @param rotationSpeed
     */
    public void setRotationSpeed(float rotationSpeed){
        this.rotationSpeed = rotationSpeed;
    }

    public void setGravity(Vector3f gravity){
        this.gravity.set(gravity);
    }

    /**
     * Registers the FirstPersonCamera to receive input events from the provided
     * InputManager.
     * @param dispacher
     */
    public void registerWithDispatcher(InputManager dispacher){
        dispacher.setCursorVisible(false);

//        dispacher.registerMouseAxisBinding("FPSCAM_Left", 0, true);
//        dispacher.registerMouseAxisBinding("FPSCAM_Right", 0, false);
//        dispacher.registerMouseAxisBinding("FPSCAM_Up", 1, false);
//        dispacher.registerMouseAxisBinding("FPSCAM_Down", 1, true);
//
//        dispacher.registerKeyBinding("FPSCAM_Left", KeyInput.KEY_LEFT);
//        dispacher.registerKeyBinding("FPSCAM_Right", KeyInput.KEY_RIGHT);
//        dispacher.registerKeyBinding("FPSCAM_Up", KeyInput.KEY_UP);
//        dispacher.registerKeyBinding("FPSCAM_Down", KeyInput.KEY_DOWN);
//
//        dispacher.registerKeyBinding("FPSCAM_StrafeLeft", KeyInput.KEY_A);
//        dispacher.registerKeyBinding("FPSCAM_StrafeRight", KeyInput.KEY_D);
//        dispacher.registerKeyBinding("FPSCAM_Forward", KeyInput.KEY_W);
//        dispacher.registerKeyBinding("FPSCAM_Backward", KeyInput.KEY_S);
//
//        dispacher.addBindingListener(this);
    }

    private void rotateCamera(float value, Vector3f axis){
        Matrix3f mat = new Matrix3f();
        mat.fromAngleNormalAxis(rotationSpeed * value, axis);

        Vector3f up = cam.getUp();
        Vector3f left = cam.getLeft();
        Vector3f dir = cam.getDirection();

        mat.mult(up, up);
        mat.mult(left, left);
        mat.mult(dir, dir);

        Quaternion q = new Quaternion();
        q.fromAxes(left, up, dir);
        q.normalize();

        cam.setAxes(q);
    }

    private void moveCamera(float value, boolean sideways){
        Vector3f vel = new Vector3f();
        Vector3f grav = new Vector3f();
        Vector3f pos = cam.getLocation().clone();

        if (sideways){
            cam.getLeft(vel);
        }else{
            cam.getDirection(vel);
        }
        vel.setY(0.001f); // prevent rising
        vel.normalizeLocal();
        vel.multLocal(value * moveSpeed);
        
        grav.set(gravity);
        grav.multLocal(FastMath.abs(value));

        if (motionAllowed != null){
            // check with gravity and velocity
            pos.addLocal(0, -grav.getY() / 2, 0);
            motionAllowed.checkMotionAllowed(pos, vel);
            pos.addLocal(0, +grav.getY() / 2, 0);
            motionAllowed.checkMotionAllowed(pos, grav);
        }else{
            pos.addLocal(vel);
            pos.addLocal(grav);
        }

        cam.setLocation(pos);
        movedThisFrame = true;
    }

    private void updateCamera(float value){
        if (!movedThisFrame){
            Vector3f pos = cam.getLocation().clone();
            Vector3f grav = new Vector3f(gravity);
            grav.multLocal(value);
            if (motionAllowed != null){
                // check with gravity
                motionAllowed.checkMotionAllowed(pos, grav);
            }else{
                pos.addLocal(grav);
            }

            cam.setLocation(pos);
        }else{
            movedThisFrame = false;
        }
    }

    public void onBinding(String binding, float value) {
        if (binding.equals("FPSCAM_Left")){
            rotateCamera(value, initialUpVec);
        }else if (binding.equals("FPSCAM_Right")){
            rotateCamera(-value, initialUpVec);
        }else if (binding.equals("FPSCAM_Up")){
            rotateCamera(-value, cam.getLeft());
        }else if (binding.equals("FPSCAM_Down")){
            rotateCamera(value, cam.getLeft());
        }else if (binding.equals("FPSCAM_Forward")){
            moveCamera(value, false);
        }else if (binding.equals("FPSCAM_Backward")){
            moveCamera(-value, false);
        }else if (binding.equals("FPSCAM_StrafeLeft")){
            moveCamera(value, true);
        }else if (binding.equals("FPSCAM_StrafeRight")){
            moveCamera(-value, true);
        }
    }

    public void onPreUpdate(float tpf) {
    }

    public void onPostUpdate(float tpf) {
        updateCamera(tpf);
    }

}
