package com.jme3.app.state;

import com.jme3.app.Application;
import com.jme3.renderer.RenderManager;

/**
 * AppState is a continously executing code inside the main loop.
 *
 * @author Kirill Vainer
 */
public interface AppState {

    public void initialize(Application app);

    public boolean isInitialized();

    public void stateAttached();

    public void stateDetached();

    public void update(float tpf);

    public void render(RenderManager rm);

    public void cleanup();

}
