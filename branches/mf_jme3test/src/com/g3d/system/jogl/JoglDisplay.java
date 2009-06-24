package com.g3d.system.jogl;

import com.g3d.input.KeyInput;
import com.g3d.input.awt.AwtKeyInput;
import com.g3d.renderer.jogl.JoglRenderer;
import com.g3d.system.AppSettings;
import com.g3d.system.AppSettings.Template;
import com.g3d.system.G3DContext.Type;
import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.TraceGL;
import javax.swing.JFrame;

public class JoglDisplay extends JoglContext implements GLEventListener {

    private static final Logger logger = Logger.getLogger(JoglDisplay.class.getName());

    protected GraphicsDevice device;
    protected GLCanvas canvas;
    protected Frame frame;
    protected Animator animator;
    protected AtomicBoolean active = new AtomicBoolean(false);
    protected AtomicBoolean windowCloseRequest = new AtomicBoolean(false);
    protected AtomicBoolean needClose = new AtomicBoolean(false);
    protected int frameRate;
    protected boolean useAwt = true;

    public Type getType() {
        return Type.Display;
    }
    
    protected DisplayMode getFullscreenDisplayMode(DisplayMode[] modes, int width, int height, int bpp, int freq){
        for (DisplayMode mode : modes){
            if (mode.getWidth() == width
             && mode.getHeight() == height
             && (mode.getBitDepth() == DisplayMode.BIT_DEPTH_MULTI || mode.getBitDepth() == bpp)
             && mode.getRefreshRate() == freq){
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

        // FIXME: seems to return false even though
        // it is supported..
//        if (!device.isDisplayChangeSupported()){
//            // must use current device mode if display mode change not supported
//            displayMode = device.getDisplayMode();
//            settings.setResolution(displayMode.getWidth(), displayMode.getHeight());
//        }

        frameRate = settings.getFrameRate();
        logger.info("Selected display mode: "+displayMode.getWidth()
                                             +"x"+displayMode.getHeight()+
                                             "x"+displayMode.getBitDepth()+
                                             " @"+displayMode.getRefreshRate());
        canvas.setSize(displayMode.getWidth(), displayMode.getHeight());

        DisplayMode prevDisplayMode = device.getDisplayMode();

        if (settings.isFullscreen() && device.isFullScreenSupported()){
            frame.setUndecorated(true);

            try{
                device.setFullScreenWindow(frame);
                if (!prevDisplayMode.equals(displayMode)
                  && device.isDisplayChangeSupported()){
                    device.setDisplayMode(displayMode);
                }
            } catch (Throwable t){
                logger.log(Level.SEVERE, "Failed to enter fullscreen mode", t);
                device.setFullScreenWindow(null);
            }
        }else{
            if (!device.isFullScreenSupported()){
                logger.warning("Fullscreen not supported.");
            }

            frame.setVisible(true);
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
        if (settings.isVSync()){
            canvas.getGL().setSwapInterval(1);
        }
        canvas.setIgnoreRepaint(true);
        canvas.addGLEventListener(this);

        Container contentPane;
        if (useAwt){
            frame = new Frame(settings.getTitle());
            contentPane = frame;
        }else{
            frame = new JFrame(settings.getTitle());
            contentPane = ((JFrame)frame).getContentPane();
        }
        frame.setResizable(false);
        frame.setFocusable(true);

        contentPane.setLayout(new BorderLayout());
        
        applySettings(settings);

        // only add canvas after frame is visible
        contentPane.add(canvas, BorderLayout.CENTER);

        if (device.getFullScreenWindow() == null){
            // now that canvas is attached,
            // determine optimal size to contain it
            frame.setSize(contentPane.getPreferredSize());
            
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setLocation((screenSize.width - frame.getWidth()) / 2,
                              (screenSize.height - frame.getHeight()) / 2);
        }

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                windowCloseRequest.set(true);
            }
            @Override
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
        renderer = new JoglRenderer(this, gl);
        super.create();

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

    public void setTitle(String title){
        if (frame != null)
            frame.setTitle(title);
    }

    /**
     * Callback.
     */
    public void init(GLAutoDrawable drawable) {
        //((JoglRenderer)renderer).setGL(drawable.getGL());
        renderer.initialize();
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

}
