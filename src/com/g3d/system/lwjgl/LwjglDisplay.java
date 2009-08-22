package com.g3d.system.lwjgl;

import com.g3d.input.JoyInput;
import com.g3d.input.KeyInput;
import com.g3d.input.MouseInput;
import com.g3d.input.lwjgl.LwjglJoyInput;
import com.g3d.input.lwjgl.LwjglKeyInput;
import com.g3d.input.lwjgl.LwjglMouseInput;
import com.g3d.system.G3DContext.Type;
import java.util.logging.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.opengl.Util;
import com.g3d.system.AppSettings;

import com.g3d.system.AppSettings.Template;
import com.g3d.system.G3DSystem;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
//import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.OpenGLException;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class LwjglDisplay extends LwjglContext implements Runnable {

    private static final Logger logger = Logger.getLogger(LwjglDisplay.class.getName());
    protected AtomicBoolean needClose = new AtomicBoolean(false);
    protected int frameRate = 0;

    protected DisplayMode getFullscreenDisplayMode(int width, int height, int bpp, int freq){
        try {
            DisplayMode[] modes = Display.getAvailableDisplayModes();
            for (DisplayMode mode : modes){
                if (mode.getWidth() == width && mode.getHeight() == height
                 && mode.getBitsPerPixel() == bpp && mode.getFrequency() == freq){
                    return mode;
                }
            }
        } catch (LWJGLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected void applySettings(AppSettings settings){
        DisplayMode displayMode = null;
        if (settings.getTemplate() == Template.DesktopFullscreen){
//            displayMode = org.lwjgl.opengl.Display.getDesktopDisplayMode();
//            settings.setResolution(displayMode.getWidth(), displayMode.getHeight());
        }else if (settings.isFullscreen()){
            displayMode = getFullscreenDisplayMode(settings.getWidth(), settings.getHeight(),
                                                   settings.getBitsPerPixel(), settings.getFrequency());
            if (displayMode == null)
                throw new RuntimeException("Unable to find fullscreen display mode matching settings");
        }else{
            displayMode = new DisplayMode(settings.getWidth(), settings.getHeight());
        }

        frameRate = settings.getFrameRate();
        logger.info("Selected display mode: "+displayMode);
        try{
            Display.setTitle(settings.getTitle());
            if (displayMode != null)
                Display.setDisplayMode(displayMode);
            
            Display.setFullscreen(settings.isFullscreen());
            Display.setVSyncEnabled(settings.isVSync());
        } catch (LWJGLException ex){
            throw new RuntimeException("Failed to create display.", ex);
        }
    }

    protected void initInThread(){
        PixelFormat pf = new PixelFormat(settings.getBitsPerPixel(),
                                         0,
                                         settings.getDepthBits(),
                                         settings.getStencilBits(),
                                         settings.getSamples());

        try{
            applySettings(settings);
            String rendererStr = settings.getString("Renderer");
//            if (rendererStr.startsWith("LWJGL-OpenGL3")){
//                ContextAttribs attribs;
//                if (rendererStr.equals("LWJGL-OpenGL3.1")){
//                    attribs = new ContextAttribs(3, 1);
//                }else{
//                    attribs = new ContextAttribs(3, 0);
//                }
//                attribs.withForwardCompatible(true);
//                attribs.withDebug(false);
//                Display.create(pf, attribs);
//            }else{
                Display.create(pf);
//                Display.create();
//            }

//            if (!Display.isFullscreen()){
//                // put it in the center
//                DisplayMode desktop = Display.getDesktopDisplayMode();
//                DisplayMode displayMode = Display.getDisplayMode();
//                Display.setLocation((desktop.getWidth() - displayMode.getWidth()) / 2,
//                                  (desktop.getHeight() - displayMode.getHeight()) / 2);
//            }

            logger.info("Display created.");
            logger.fine("Running on thread: "+Thread.currentThread().getName());

//            try{
//                 Util.checkGLError();
//            } catch (OpenGLException ex){
//                System.out.println(ex.getMessage());
//            }

            Keyboard.poll();
            Mouse.poll();

            String vendor = glGetString(GL_VENDOR);
            String version = glGetString(GL_VERSION);
            String renderer = glGetString(GL_RENDERER);
            String shadingLang = glGetString(GL_SHADING_LANGUAGE_VERSION);

            logger.info("Vendor: "+vendor);
            logger.info("Renderer: "+renderer);

            logger.info("Adapter: "+Display.getAdapter());
            logger.info("Driver Version: "+Display.getVersion());

            logger.info("OpenGL Version: "+version);
            logger.info("GLSL Ver: "+shadingLang);

            created.set(true);
        } catch (LWJGLException ex){
            G3DSystem.reportError("Failed to create display.", ex);
        } finally {
            // TODO: It is possible to avoid "Failed to find pixel format"
            // error here by creating a default display.

            if (!created.get()){
                if (Display.isCreated())
                    Display.destroy();
            }
        }
        super.create();
        listener.initialize();
    }

    protected void runLoop(){
        if (!created.get())
            throw new IllegalStateException();

        listener.update();
        
        // calls swap buffers, etc.
        try {
            Display.update();
        } catch (Throwable ex){
            logger.log(Level.WARNING, "Error while swapping buffers", ex);
        }

        if (frameRate > 0)
            Display.sync(frameRate);

        renderer.onFrame();
    }

    protected void deinitInThread(){
        listener.destroy();

        renderer.cleanup();
        Display.destroy();
        logger.info("Display destroyed.");
        super.destroy();
    }

    @Override
    public void destroy(){
        needClose.set(true);
    }

    @Override
    public boolean isActive() {
        return Display.isActive();
    }

    @Override
    public void restart(boolean updateCamera) {
        if (created.get()){
            applySettings(settings);
            if (renderer.getCamera() != null && updateCamera){
                renderer.getCamera().resize(settings.getWidth(), settings.getHeight(), true);
            }
            logger.info("Display restarted.");
        }else{
            logger.warning("Display is not created, cannot restart window.");
        }
    }

    public Type getType() {
        return Type.Display;
    }

    public boolean isCloseRequested() {
        return Display.isCloseRequested();
    }

    public void setTitle(String title){
        if (created.get())
            Display.setTitle(title);
    }

    public void run(){
        logger.info("Using LWJGL "+Sys.getVersion());
        initInThread();
        while (true){
            if (needClose.get())
                break;

            runLoop();
        }
        deinitInThread();
    }

    @Override
    public void create(){
        if (created.get()){
            logger.warning("create() called when display is already created!");
            return;
        }

        new Thread(this, "LWJGL Renderer Thread").start();
    }

    public JoyInput getJoyInput() {
        return new LwjglJoyInput();
    }

    public MouseInput getMouseInput() {
        return new LwjglMouseInput();
    }

    public KeyInput getKeyInput() {
        return new LwjglKeyInput();
    }

}
