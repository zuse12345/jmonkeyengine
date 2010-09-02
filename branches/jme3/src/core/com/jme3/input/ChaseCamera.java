package com.jme3.input;

import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;

public class ChaseCamera implements ActionListener, AnalogListener {

    private Spatial target = null;
    private float distance = 20;
    private float minHeight = 0.05f;
    private float maxHeight = FastMath.PI / 2;
    private float minDistance = 1.0f;
    private float maxDistance = 40.0f;
    private float zoomSpeed = 2.0f;
    private float rotationSpeed = 1.0f;

    /**
     * the camera.
     */
    private Camera cam = null;
    private InputManager inputManager;
    private Vector3f initialUpVec;
    private boolean canRotate;
    private float rotation = FastMath.PI / 2;
    private float vRotation = 0;

    private boolean enabled = true;

    public ChaseCamera(Camera cam, final Spatial target) {
        this.target = target;
        this.cam = cam;
        initialUpVec = cam.getUp().clone();
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
        } else if (name.equals("ZoomOut")) {
            zoomCamera(-value);
        }
    }

    public void registerWithInput(InputManager inputManager) {
        String[] inputs = {"toggleRotate", "Down", "Up", "mouseLeft", "mouseRight", "ZoomIn", "ZoomOut"};

        this.inputManager = inputManager;

        inputManager.addMapping("Down",         new MouseAxisTrigger(1, true));
        inputManager.addMapping("Up",           new MouseAxisTrigger(1, false));
        inputManager.addMapping("ZoomIn",       new MouseAxisTrigger(2, true));
        inputManager.addMapping("ZoomOut",      new MouseAxisTrigger(2, false));
        inputManager.addMapping("mouseLeft",    new MouseAxisTrigger(0, true));
        inputManager.addMapping("mouseRight",   new MouseAxisTrigger(0, false));
        inputManager.addMapping("toggleRotate", new MouseButtonTrigger(0));
        inputManager.addMapping("toggleRotate", new MouseButtonTrigger(1));

        inputManager.addListener(this, inputs);

        updateCamera();
    }

    private void rotateCamera(float value) {
        if (!canRotate || !enabled) {
            return;
        }
        
        rotation += value * rotationSpeed;
        updateCamera();
    }

    private void zoomCamera(float value) {
        if (!enabled)
            return;

        distance += value * zoomSpeed;
        if (distance > maxDistance) {
            distance = maxDistance;
        }
        if (distance < minDistance) {
            distance = minDistance;
        }
        if ((vRotation < minHeight) && (distance > (minDistance + 1.0f))) {
            vRotation = minHeight;
        }

        updateCamera();
    }

    private void vRotateCamera(float value) {
        if (!canRotate || !enabled) {
            return;
        }
        vRotation += value * rotationSpeed;
        if (vRotation > maxHeight) {
            vRotation = maxHeight;
        }
        if ((vRotation < minHeight) && (distance > (minDistance + 1.0f))) {
            vRotation = minHeight;
        }
        updateCamera();
    }

    public void updateCamera() {
        float hDistance = distance * FastMath.sin((FastMath.PI / 2) - vRotation);
        Vector3f pos = new Vector3f(hDistance * FastMath.cos(rotation), distance * FastMath.sin(vRotation), hDistance * FastMath.sin(rotation));
        pos = pos.add(target.getLocalTranslation());
        cam.setLocation(pos);
        cam.lookAt(target.getLocalTranslation(), initialUpVec);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled)
            canRotate = false; // reset this flag in-case it was on before
    }
    
    public float getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
    }

    public float getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(float minDistance) {
        this.minDistance = minDistance;
    }

}