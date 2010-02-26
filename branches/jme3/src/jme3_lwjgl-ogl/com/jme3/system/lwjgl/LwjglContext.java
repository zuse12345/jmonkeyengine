package com.jme3.system.lwjgl;

import com.jme3.renderer.Renderer;
import com.jme3.renderer.lwjgl.LwjglRenderer;
import com.jme3.system.AppSettings;
import com.jme3.system.SystemListener;
import com.jme3.system.G3DContext;
import com.jme3.system.Timer;
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
        renderer = new LwjglRenderer();
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
