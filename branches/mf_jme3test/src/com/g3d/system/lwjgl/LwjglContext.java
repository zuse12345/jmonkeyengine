package com.g3d.system.lwjgl;

import com.g3d.renderer.Renderer;
import com.g3d.renderer.lwjgl.LwjglRenderer;
import com.g3d.system.DisplaySettings;
import com.g3d.system.DisplaySettings.Template;
import com.g3d.system.G3DContext;
import com.g3d.system.Timer;
import org.lwjgl.opengl.DisplayMode;

/**
 * A LWJGL implementation of a graphics context.
 */
public abstract class LwjglContext implements G3DContext {

    protected boolean created = false;
    protected DisplaySettings settings;
    protected LwjglRenderer renderer;
    protected Timer timer;

    public abstract boolean isActive();
    public abstract void restart();

    public void destroy(){
        created = false;
        timer = null;
    }
    
    public void create(){
        timer = Timer.getTimer();
        renderer = new LwjglRenderer();
        created = true;
    }

    public void setDisplaySettings(DisplaySettings settings) {
        this.settings = settings;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public Timer getTimer() {
        return timer;
    }

}
