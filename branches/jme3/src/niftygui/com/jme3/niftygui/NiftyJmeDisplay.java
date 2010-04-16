package com.jme3.niftygui;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.input.InputManager;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.render.NiftyRenderEngine;
import de.lessvoid.nifty.render.NiftyRenderEngineImpl;
import de.lessvoid.nifty.sound.SoundSystem;
import de.lessvoid.nifty.tools.TimeProvider;

public class NiftyJmeDisplay extends TimeProvider implements SceneProcessor {

    private boolean inited = false;
    private Nifty nifty;
    private AssetManager assetManager;
    private RenderManager renderManager;
    private RenderDeviceJme renderDev;
    private InputSystemJme inputSys;
    private SoundDeviceJme soundDev;
    private ViewPort vp;

    private int w, h;

    public NiftyJmeDisplay(AssetManager assetManager, 
                           InputManager inputManager,
                           AudioRenderer audioRenderer,
                           ViewPort vp){
        this.assetManager = assetManager;

        w = vp.getCamera().getWidth();
        h = vp.getCamera().getHeight();

        soundDev = new SoundDeviceJme(assetManager, audioRenderer);
        renderDev = new RenderDeviceJme(this);
        inputSys = new InputSystemJme();
        inputManager.addRawInputListener(inputSys);
        NiftyRenderEngine renderEngine = new NiftyRenderEngineImpl(renderDev);

        nifty = new Nifty(
                renderEngine,
                new SoundSystem(soundDev),
                inputSys,
                new TimeProvider());
    }

    public void initialize(RenderManager rm, ViewPort vp) {
        this.renderManager = rm;
        renderDev.setRenderManager(rm);
        inited = true;
        this.vp = vp;
    }

    public Nifty getNifty() {
        return nifty;
    }

    RenderDeviceJme getRenderDevice() {
        return renderDev;
    }

    AssetManager getAssetManager() {
        return assetManager;
    }

    RenderManager getRenderManager() {
        return renderManager;
    }

    int getHeight() {
        return h;
    }

    int getWidth() {
        return w;
    }

    public void reshape(ViewPort vp, int w, int h) {
        this.w = w;
        this.h = h;
    }

    public boolean isInitialized() {
        return inited;
    }

    public void preFrame(float tpf) {
    }

    public void postQueue(RenderQueue rq) {
        // render nifty before anything else
        renderManager.setCamera(vp.getCamera(), true);
        nifty.render(false);
        renderManager.setCamera(vp.getCamera(), false);

        if (RenderDeviceJme.GUI_DEBUG)
            System.exit(1);
    }

    public void postFrame(FrameBuffer out) {
    }

    public void cleanup() {
        inited = false;
        nifty.exit();
    }

}
