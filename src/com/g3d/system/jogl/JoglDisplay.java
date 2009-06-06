package com.g3d.system.jogl;

import com.g3d.math.Matrix4f;
import com.g3d.math.Quaternion;
import com.g3d.renderer.Camera;
import com.g3d.renderer.jogl.JoglRenderer;
import com.g3d.system.AppSettings;
import com.g3d.system.AppSettings.Template;
import com.g3d.system.G3DContext.Type;
import com.g3d.system.G3DSystem;
import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLEventListener;
import javax.swing.JFrame;

public class JoglDisplay extends JoglContext implements GLEventListener {

    private static final Logger logger = Logger.getLogger(JoglDisplay.class.getName());

    protected GraphicsDevice device;
    protected GLCanvas canvas;
    protected JFrame frame;
    protected Animator animator;
    protected AtomicBoolean active = new AtomicBoolean(false);
    protected AtomicBoolean windowCloseRequest = new AtomicBoolean(false);
    protected AtomicBoolean needClose = new AtomicBoolean(false);

    public Type getType() {
        return Type.Display;
    }
    
    protected DisplayMode getFullscreenDisplayMode(DisplayMode[] modes, int width, int height, int bpp, int freq){
        for (DisplayMode mode : modes){
            if (mode.getWidth() == width && mode.getHeight() == height
             && mode.getBitDepth() == bpp && mode.getRefreshRate() == freq){
                return mode;
            }
        }
        return null;
    }
    
    protected void applySettings(AppSettings settings){
        DisplayMode displayMode;
        if (settings.getTemplate() == Template.DesktopFullscreen){
            displayMode = device.getDisplayMode();
            settings.setResolution(displayMode.getWidth(), displayMode.getHeight());
        }else if (settings.isFullscreen()){
            displayMode = getFullscreenDisplayMode(device.getDisplayModes(),
                                                   settings.getWidth(), settings.getHeight(),
                                                   settings.getBitsPerPixel(), settings.getFrequency());
            if (displayMode == null)
                throw new RuntimeException("Unable to find fullscreen display mode matching settings");
        }else{
            displayMode = new DisplayMode(settings.getWidth(), settings.getHeight(), 0, 0);
        }

        logger.info("Selected display mode: "+displayMode);
        canvas.setSize(displayMode.getWidth(), displayMode.getHeight());
        if (settings.isFullscreen() && device.isFullScreenSupported()){

            frame.setUndecorated(true);
            device.setDisplayMode(displayMode);
        }else{
            if (!device.isFullScreenSupported()){
                logger.warning("Fullscreen not supported.");
            }

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setSize(frame.getContentPane().getPreferredSize());
            frame.setLocation((screenSize.width - frame.getWidth()) / 2,
                              (screenSize.height - frame.getHeight()) / 2);
        }
    }

    @Override
    public void create(){
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
        canvas.setIgnoreRepaint(true);
        canvas.addGLEventListener(this);

        frame = new JFrame(settings.getTitle());
        frame.setResizable(false);
        frame.getContentPane().setLayout( new BorderLayout() );
        frame.getContentPane().add( canvas, BorderLayout.CENTER );

        animator = new Animator(canvas);
        animator.setRunAsFastAsPossible(false);

        applySettings(settings);

        if (settings.isFullscreen()){
            device.setFullScreenWindow(frame);
        }else{
            frame.setVisible(true);
        }

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                windowCloseRequest.set(true);
            }
            public void windowClosed(WindowEvent evt) {
            }

            @Override
            public void windowActivated(WindowEvent evt) {
                active.set(true);
            }

            @Override
            public void windowDeactivated(WindowEvent evt) {
                active.set(false);
            }
        });

        super.create();

        canvas.requestFocus();
        animator.start();

        logger.info("Display created.");
    }

    /**
     * Callback.
     */
    public void init(GLAutoDrawable drawable) {
        ((JoglRenderer)renderer).setGL(drawable.getGL());
        listener.initialize();
    }

    /**
     * Callback.
     */
    public void display(GLAutoDrawable drawable) {
        if (needClose.get()) {
            animator.stop();
            if (settings.isFullscreen()) {
                device.setFullScreenWindow(null);
            }
            frame.dispose();
            logger.info("Display destroyed.");
            super.internalDestroy();
            return;
        }

        listener.update();
        renderer.onFrame();
        windowCloseRequest.set(false);
    }

    /**
     * Callback.
     */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    }

    /**
     * Callback.
     */
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    public void destroy(){
        needClose.set(true);
    }

    public boolean isCloseRequested() {
        return windowCloseRequest.get();
    }

    public boolean isActive() {
        return active.get();
    }

    public void restart(boolean updateCamera) {
    }

}
