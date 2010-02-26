package com.jme3.system.lwjgl;

import com.jme3.system.AppSettings;
import com.jme3.system.G3DCanvasContext;
import com.jme3.system.G3DContext.Type;
import java.awt.Canvas;
import java.util.logging.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

public class LwjglCanvas extends LwjglAbstractDisplay implements G3DCanvasContext {

    private static final Logger logger = Logger.getLogger(LwjglDisplay.class.getName());
    private Canvas canvas;
    private int width;
    private int height;

    public LwjglCanvas(){
        super();
        canvas = new Canvas();
        canvas.setFocusable(true);
        canvas.setIgnoreRepaint(true);
    }

    protected void runLoop(){
        if (width != canvas.getWidth() || height != canvas.getHeight()){
            width = canvas.getWidth();
            height = canvas.getHeight();
            if (listener != null)
                listener.reshape(width, height);
        }
        super.runLoop();

        if (!canvas.isDisplayable()){
            destroy(); // native peer destroyed
        }
    }

    @Override
    public Type getType() {
        return Type.Canvas;
    }

    @Override
    public void setTitle(String title) {
    }

    @Override
    public void restart() {
    }

    public Canvas getCanvas(){
        return canvas;
    }

    @Override
    protected void applySettings(AppSettings settings) {
        frameRate = settings.getFrameRate();
        Display.setVSyncEnabled(settings.isVSync());

        try{
            Display.setParent(canvas);
        }catch (LWJGLException ex){
            listener.handleError("Failed to parent canvas to display", ex);
        }
    }

}
