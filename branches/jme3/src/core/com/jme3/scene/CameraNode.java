package com.jme3.scene;

import com.jme3.renderer.Camera;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.control.CameraControl.ControlDirection;

/**
 * This Node is a shorthand for using a CameraControl.
 *
 * @author Tim8Dev
 */
public class CameraNode extends Node {
    private CameraControl camControl;

    /**
     * for IO purpose
     */
    public CameraNode() {}
    public CameraNode(Camera camera) {
        this("defCamNodeName", camera);
    }
    public CameraNode(CameraControl control) {
        this("defCamNodeName", control);
    }
    public CameraNode(String name, Camera camera){
        this(name, new CameraControl(camera));
    }
    public CameraNode(String name, CameraControl control) {
        super(name);
        addControl(control);
        camControl = control;
    }

    public void setEnabled(boolean enabled) {
        camControl.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return camControl.isEnabled();
    }

    public void setControlDir(ControlDirection controlDir) {
        camControl.setControlDir(controlDir);
    }

    public void setCamera(Camera camera) {
        camControl.setCamera(camera);
    }

    public ControlDirection getControlDir() {
        return camControl.getControlDir();
    }

    public Camera getCamera() {
        return camControl.getCamera();
    }
}