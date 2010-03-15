package com.jme3.system.jogl;

import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.awt.AwtKeyInput;
import com.jme3.input.awt.AwtMouseInput;
import com.jme3.renderer.jogl.JoglRenderer;
import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.TraceGL;

public abstract class JoglAbstractDisplay extends JoglContext implements GLEventListener {

    private static final Logger logger = Logger.getLogger(JoglAbstractDisplay.class.getName());

    protected GraphicsDevice device;
    protected GLCanvas canvas;
    protected Animator animator;
    protected AtomicBoolean active = new AtomicBoolean(false);
    
    protected boolean wasActive = false;
    protected int frameRate;
    protected boolean useAwt = true;
    protected boolean autoFlush = true;

    protected void initGLCanvas(){
        device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        GLCapabilities caps = new GLCapabilities();
        caps.setHardwareAccelerated(true);
        caps.setDoubleBuffered(true);
        caps.setStencilBits(settings.getStencilBits());
        caps.setDepthBits(settings.getDepthBits());

        if (settings.getSamples() > 0){
            caps.setSampleBuffers(true);
            caps.setNumSamples(settings.getSamples());
        }

        canvas = new GLCanvas(caps);
        if (settings.isVSync()){
            canvas.getGL().setSwapInterval(1);
        }
        canvas.setFocusable(true);
        canvas.setIgnoreRepaint(true);
        canvas.addGLEventListener(this);

        GL gl = canvas.getGL();
        if (false){
            // trace mode
            // jME already uses err stream, use out instead
            gl = new TraceGL(gl, System.out);
        }else if (false){
            // debug mode
            gl = new DebugGL(gl);
        }else{
            // production mode
        }
        renderer = new JoglRenderer(gl);
        super.create();
    }

    protected void startGLCanvas(){
        if (frameRate > 0){
            animator = new FPSAnimator(canvas, frameRate);
            animator.setRunAsFastAsPossible(true);
        }else{
            animator = new Animator(canvas);
            animator.setRunAsFastAsPossible(true);
        }

        canvas.requestFocus();
        animator.start();

        logger.info("Display created.");
    }

    @Override
    public KeyInput getKeyInput(){
        return new AwtKeyInput(canvas);
    }

    @Override
    public MouseInput getMouseInput(){
        return new AwtMouseInput(canvas);
    }

    /**
     * Callback.
     */
    public void init(GLAutoDrawable drawable) {
        //((JoglRenderer)renderer).setGL(drawable.getGL());
        renderer.initialize();
        listener.initialize();
    }

    public void setAutoFlushFrames(boolean enabled){
        autoFlush = enabled;
    }

    /**
     * Callback.
     */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        listener.reshape(width, height);
    }

    /**
     * Callback.
     */
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

}
