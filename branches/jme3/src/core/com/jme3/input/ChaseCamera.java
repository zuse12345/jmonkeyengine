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
    private float distance = 20;
    private float minHeight = 0.00f;
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

    /**
     * Constructs the chase camera
     * @param cam the application camera
     * @param target the spatial to follow
     */
    public ChaseCamera(Camera cam, final Spatial target) {
        this.target = target;
        target.addControl(this);
        this.cam = cam;
        initialUpVec = cam.getUp().clone();
    }

    /**
     * Constructs the chase camera, and registers inputs
     * @param cam the application camera
     * @param target the spatial to follow
     * @param inputManager the inputManager of the application to register inputs
     */
    public ChaseCamera(Camera cam, final Spatial target, InputManager inputManager) {
        this.target = target;
        target.addControl(this);
        this.cam = cam;
        initialUpVec = cam.getUp().clone();
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
        inputManager.addMapping("toggleRotate", new MouseButtonTrigger(0));
        inputManager.addMapping("toggleRotate", new MouseButtonTrigger(1));

        inputManager.addListener(this, inputs);

        updateCamera();
    }

    //rotate the camera around the target on the horizontal plane
    private void rotateCamera(float value) {
        if (!canRotate || !enabled) {
            return;
        }
        rotation += value * rotationSpeed;
        updateCamera();
    }

    //move the camera toward or away the target
    private void zoomCamera(float value) {
        if (!enabled) {
            return;
        }

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

    //rotate the camera around the target on the vertical plane
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

    /**
     * Update the camera, should only be called internally
     */
    public void updateCamera() {
        float hDistance = distance * FastMath.sin((FastMath.PI / 2) - vRotation);
        Vector3f pos = new Vector3f(hDistance * FastMath.cos(rotation), distance * FastMath.sin(vRotation), hDistance * FastMath.sin(rotation));
        pos = pos.add(target.getLocalTranslation());
        cam.setLocation(pos);
        cam.lookAt(target.getLocalTranslation(), initialUpVec);
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
        updateCamera();
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


}
