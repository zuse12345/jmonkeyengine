package com.jme3.app.state;

import com.jme3.app.Application;
import com.jme3.renderer.RenderManager;

public class AbstractAppState implements AppState {

    private boolean initialized = false;

    public void initialize(Application app) {
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void stateAttached() {
    }

    public void stateDetached() {
    }

    public void update(float tpf) {
    }

    public void render(RenderManager rm) {
    }

    public void cleanup() {
        initialized = false;
    }

}
