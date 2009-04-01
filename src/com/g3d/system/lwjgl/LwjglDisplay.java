package com.g3d.system.lwjgl;

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
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;

public class LwjglDisplay extends LwjglContext {

    private static final Logger logger = Logger.getLogger(LwjglDisplay.class.getName());

    protected DisplayMode getFullscreenDisplayMode(int width, int height, int bpp, int freq){
        DisplayMode[] disp = null;
        try {
            disp = org.lwjgl.util.Display.getAvailableDisplayModes(width, height, width, height, bpp, bpp, freq, freq);
        } catch (LWJGLException ex) {
            ex.printStackTrace();
        }
        if (disp == null || disp.length == 0)
            return null;

        return disp[0];
    }

    protected void applySettings(DisplaySettings settings){
        DisplayMode displayMode = null;
        if (settings.getTemplate() == Template.DesktopFullscreen){
            displayMode = org.lwjgl.opengl.Display.getDesktopDisplayMode();
        }else if (settings.isFullscreen()){
            displayMode = getFullscreenDisplayMode(settings.getWidth(), settings.getHeight(),
                                                   settings.getBitsPerPixel(), settings.getFrequency());
            if (displayMode == null)
                throw new RuntimeException("Unable to find fullscreen display mode matching settings");
        }else{
            displayMode = new DisplayMode(settings.getWidth(), settings.getHeight());
        }
        logger.fine("Selected display mode: "+displayMode);

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

        PixelFormat pf = new PixelFormat(settings.getBitsPerPixel(),
                                         0,
                                         settings.getDepthBits(),
                                         settings.getStencilBits(),
                                         settings.getSamples());

        try{
            applySettings(settings);
            Display.create(pf);

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

//            if (!GLContext.getCapabilities().OpenGL30){
//                throw new UnsupportedOperationException("Your driver does not support OpenGL 3");
//            }else{
//                logger.fine("OpenGL 3.0 supported!");
//            }

            logger.fine("Running on thread: "+Thread.currentThread().getName());

            Util.checkGLError();

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

    @Override
    public void destroy(){
        Display.destroy();
        logger.info("Display destroyed.");
        super.destroy();
    }

    @Override
    public boolean isActive() {
        return Display.isActive();
    }

    @Override
    public void restart() {
        if (created){
            applySettings(settings);
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
