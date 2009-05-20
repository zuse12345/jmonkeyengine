package com.g3d.system.lwjgl;

import com.g3d.input.JoyInput;
import com.g3d.input.KeyInput;
import com.g3d.input.MouseInput;
import com.g3d.input.lwjgl.LwjglJoyInput;
import com.g3d.input.lwjgl.LwjglKeyInput;
import com.g3d.input.lwjgl.LwjglMouseInput;
import com.g3d.renderer.GLObjectManager;
import com.g3d.renderer.lwjgl.LwjglRenderer;
import com.g3d.system.G3DContext.Type;
import com.g3d.util.TempVars;
import java.nio.IntBuffer;
import java.util.logging.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.opengl.Util;
import com.g3d.system.DisplaySettings;

import com.g3d.system.DisplaySettings.Template;
import com.g3d.system.G3DSystem;
import java.util.logging.Level;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.OpenGLException;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;

public class LwjglDisplay extends LwjglContext {

    private static final Logger logger = Logger.getLogger(LwjglDisplay.class.getName());

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

    protected void applySettings(DisplaySettings settings){
        DisplayMode displayMode = null;
        if (settings.getTemplate() == Template.DesktopFullscreen){
            displayMode = org.lwjgl.opengl.Display.getDesktopDisplayMode();
            settings.setResolution(displayMode.getWidth(), displayMode.getHeight());
        }else if (settings.isFullscreen()){
            displayMode = getFullscreenDisplayMode(settings.getWidth(), settings.getHeight(),
                                                   settings.getBitsPerPixel(), settings.getFrequency());
            if (displayMode == null)
                throw new RuntimeException("Unable to find fullscreen display mode matching settings");
        }else{
            displayMode = new DisplayMode(settings.getWidth(), settings.getHeight());
        }

        logger.info("Selected display mode: "+displayMode);
        try{
            Display.setVSyncEnabled(settings.isVSync());
            Display.setFullscreen(settings.isFullscreen());
            Display.setDisplayMode(displayMode);
            Display.setTitle(settings.getTitle());
        } catch (LWJGLException ex){
            throw new RuntimeException("Failed to create display.", ex);
        } finally {
            if (!created){
                if (Display.isCreated())
                    Display.destroy();
            }
        }
    }

    @Override
    public void create(){
        if (created){
            logger.warning("create() called when display is already created!");
            return;
        }

        logger.info("Using LWJGL "+Sys.getVersion());

        PixelFormat pf = new PixelFormat(settings.getBitsPerPixel(),
                                         0,
                                         settings.getDepthBits(),
                                         settings.getStencilBits(),
                                         settings.getSamples());

        try{
            applySettings(settings);
            String rendererStr = settings.getString("Renderer");
            if (rendererStr.equals("LWJGL-OpenGL3")){
                ContextAttribs attribs = new ContextAttribs(3, 0);
                attribs.withForwardCompatible(true);
                Display.create(pf, attribs);
            }else{
                Display.create(pf);
            }

            boolean isActive = Display.isActive();
            boolean isCreated = Display.isCreated();
            boolean isVisible = Display.isVisible();
            if (isActive && isCreated && isVisible)
                logger.info("Display created.");
            else{
                if (!isVisible)
                    logger.warning("Display is not visible!");
                if (!isCreated)
                    logger.warning("Display is not created!");
                if (!isActive)
                    logger.warning("Display is not active!");
            }

            if (GLContext.getCapabilities().OpenGL30){
                logger.fine("OpenGL 3.0 supported!");
            }

            logger.fine("Running on thread: "+Thread.currentThread().getName());
            
            try{
                 Util.checkGLError();
            } catch (OpenGLException ex){
                System.out.println(ex.getMessage());
            }

            IntBuffer temp = TempVars.get().intBuffer16;
            if (settings.getSamples() != 0) {
                glEnable(GL_MULTISAMPLE);
                logger.info("Multisampling enabled. ");

                glGetInteger(GL_SAMPLE_BUFFERS, temp);
                logger.info("Sample buffers: "+temp.get(0));

                glGetInteger(GL_SAMPLES, temp);
                logger.info("Samples: "+temp.get(0));
            }

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

            created = true;
        } catch (LWJGLException ex){
            G3DSystem.reportError("Failed to create display.", ex);
        } finally {
            if (!created){
                if (Display.isCreated())
                    Display.destroy();
            }
        }
        super.create();
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

    @Override
    public void destroy(){
        renderer.cleanup();
        Display.destroy();
        logger.info("Display destroyed.");
        super.destroy();
    }

    @Override
    public boolean isActive() {
        return Display.isActive();
    }

    @Override
    public void restart(boolean updateCamera) {
        if (created){
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

    public void update() {
        if (!created)
            throw new IllegalStateException();

        Display.update();
        renderer.onFrame();
    }

}
