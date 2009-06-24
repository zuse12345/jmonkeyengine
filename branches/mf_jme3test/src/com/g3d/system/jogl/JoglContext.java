package com.g3d.system.jogl;

import com.g3d.input.JoyInput;
import com.g3d.input.KeyInput;
import com.g3d.input.MouseInput;
import com.g3d.input.awt.AwtKeyInput;
import com.g3d.input.dummy.DummyKeyInput;
import com.g3d.input.dummy.DummyMouseInput;
import com.g3d.renderer.Renderer;
import com.g3d.renderer.jogl.JoglRenderer;
import com.g3d.renderer.lwjgl.LwjglRenderer;
import com.g3d.system.AppSettings;
import com.g3d.system.ContextListener;
import com.g3d.system.G3DContext;
import com.g3d.system.NanoTimer;
import com.g3d.system.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class JoglContext implements G3DContext {

    protected AtomicBoolean created = new AtomicBoolean(false);
    protected AppSettings settings = new AppSettings();
    protected JoglRenderer renderer;
    protected Timer timer;
    protected ContextListener listener;

    protected AwtKeyInput keyInput;

    public void setContextListener(ContextListener listener){
        this.listener = listener;
    }

    public void setSettings(AppSettings settings) {
        this.settings.copyFrom(settings);
    }

    public AppSettings getSettings() {
        return settings;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public MouseInput getMouseInput() {
        return new DummyMouseInput();
    }

    public KeyInput getKeyInput() {
        return keyInput;
    }

    public JoyInput getJoyInput() {
        return null;
    }

    public Timer getTimer() {
        return timer;
    }

    public boolean isCreated() {
        return created.get();
    }

    public void create() {
        timer = new NanoTimer();
        created.set(true);
        // renderer initialization must happen in subclass.
    }

    protected void internalDestroy() {
        created.set(false);
        renderer = null;
        timer = null;
    }

}
