package com.g3d.system.lwjgl;

import com.g3d.renderer.Renderer;
import com.g3d.renderer.lwjgl.LwjglRenderer;
import com.g3d.system.AppSettings;
import com.g3d.system.SystemListener;
import com.g3d.system.G3DContext;
import com.g3d.system.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A LWJGL implementation of a graphics context.
 */
public abstract class LwjglContext implements G3DContext {

    protected AtomicBoolean created = new AtomicBoolean(false);
    protected AppSettings settings = new AppSettings(true);
    protected LwjglRenderer renderer;
    protected Timer timer;
    protected SystemListener listener;

    public abstract void restart(boolean updateCamera);

    public void setSystemListener(SystemListener listener){
        this.listener = listener;
    }

    public void destroy(){
        created.set(false);
        renderer = null;
        timer = null;
    }
    
    public void create(){
        timer = new LwjglTimer();
        renderer = new LwjglRenderer(this);
        renderer.initialize();
        created.set(true);
    }

    public boolean isCreated(){
        return created.get();
    }

    public void setSettings(AppSettings settings) {
        this.settings.copyFrom(settings);
    }

    public AppSettings getSettings(){
        return settings;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public Timer getTimer() {
        return timer;
    }

}
